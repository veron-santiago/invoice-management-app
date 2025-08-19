package io.github.veron_santiago.backend.service.implementation;

import io.github.veron_santiago.backend.persistence.entity.Bill;
import io.github.veron_santiago.backend.persistence.entity.BillLine;
import io.github.veron_santiago.backend.persistence.repository.IBillRepository;
import io.github.veron_santiago.backend.service.exception.ErrorMessages;
import io.github.veron_santiago.backend.service.exception.InternalServerException;
import io.github.veron_santiago.backend.service.exception.ObjectNotFoundException;
import io.github.veron_santiago.backend.service.interfaces.ICompanyService;
import io.github.veron_santiago.backend.service.interfaces.IPdfService;
import io.github.veron_santiago.backend.util.AuthUtil;
import io.github.veron_santiago.backend.util.ByteArrayMultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDPushButton;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PdfServiceImpl implements IPdfService {

    private final IBillRepository billRepository;
    private final ICompanyService companyService;
    private final CloudinaryService cloudinaryService;
    private final AuthUtil authUtil;

    public PdfServiceImpl(IBillRepository billRepository, ICompanyService companyService, CloudinaryService cloudinaryService, AuthUtil authUtil) {
        this.billRepository = billRepository;
        this.companyService = companyService;
        this.cloudinaryService = cloudinaryService;
        this.authUtil = authUtil;
    }

    @Override
    public String generateBillPdf(Bill bill, HttpServletRequest request, boolean includeQr, byte[] qrBytes) throws IOException {
        try (PDDocument template = getTemplate()) {
            PDDocumentCatalog catalog = template.getDocumentCatalog();
            PDAcroForm form = catalog.getAcroForm();
            if (form == null) throw new IllegalStateException(ErrorMessages.PDF_GENERATE_ERROR.getMessage());
            form.setNeedAppearances(false);

            String billNumber = getBillNumber(bill.getBillNumber());
            Long companyId = bill.getCompany().getId();
            PDResources resources = form.getDefaultResources();
            if (resources == null) {
                resources = new PDResources();
                form.setDefaultResources(resources);
            }
            PDType1Font helvetica = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font helveticaBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            COSName helv = resources.add(helvetica);
            COSName helvBold = resources.add(helveticaBold);
            Set<String> boldFields = Set.of(
                    "companyNameTittle",
                    "companyName",
                    "customerName",
                    "totalAmount"
            );
            for (PDField fld : form.getFields()) {
                if (!(fld instanceof PDTextField textField)) continue;
                String name = textField.getPartialName();
                String originalDA = textField.getDefaultAppearance();
                Matcher m = Pattern.compile(".*\\s(\\d+(?:\\.\\d+)?)\\sTf.*").matcher(originalDA);
                String size = m.matches() ? m.group(1) : "12";
                if (boldFields.contains(name)) {
                    textField.setDefaultAppearance("/" + helvBold.getName() + " " + size + " Tf 0 g");
                } else {
                    textField.setDefaultAppearance("/" + helv.getName() + " " + size + " Tf 0 g");
                }
            }

            String logo = companyService.getLogo(request);
            if (logo != null && !logo.isBlank()){
                URI uri = new URI(logo);
                URL url = uri.toURL();
                try (InputStream in = url.openStream()) {
                    BufferedImage buffered = ImageIO.read(in);
                    PDImageXObject pdImage = LosslessFactory.createFromImage(template, buffered);
                    setImageInAcroButton(template, pdImage, form, "logo_af_image", true);
                }
            }

            if (includeQr && qrBytes != null) {
                PDImageXObject qrImage = PDImageXObject.createFromByteArray(template, qrBytes, "qr-code");
                setImageInAcroButton(template, qrImage, form, "qr_af_image", false);
            }


            form.getField("companyNameTittle").setValue(bill.getCompanyName());
            form.getField("companyInfo").setValue(getCompanyInfo(bill));
            form.getField("customerInfo").setValue(getCustomerInfo(bill));
            form.getField("billNumber").setValue(billNumber);
            form.getField("issue_af_date").setValue(dateFormatter(bill.getIssueDate()));
            if (bill.getDueDate() != null){
                form.getField("due_af_date").setValue(dateFormatter(bill.getDueDate()));
            }
            form.getField("companyName").setValue(bill.getCompanyName());
            form.getField("customerName").setValue(bill.getCustomerName());
            form.getField("totalAmount").setValue("$ " + formatAmount(bill.getTotalAmount()));
            saveBillLines(bill.getBillLines(), form);
            form.flatten();

            return savePdf(template, companyId, billNumber);
        } catch (URISyntaxException e) {
            throw new InternalServerException("Error al generar el PDF");
        }
    }

    @Override
    public String getPdfByBillId(Long billId, HttpServletRequest request) throws AccessDeniedException {

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ObjectNotFoundException(ErrorMessages.BILL_NOT_FOUND.getMessage()));
        Long companyId = bill.getCompany().getId();
        if (!Objects.equals(companyId, authUtil.getAuthenticatedCompanyId(request))) {
            throw new AccessDeniedException(ErrorMessages.ACCESS_DENIED_READ.getMessage());
        }

        String pdfPath = bill.getPdfPath();
        if (pdfPath != null && !pdfPath.isEmpty()) return pdfPath;
        else throw new ObjectNotFoundException("PDF no encontrado");
    }

    private PDDocument getTemplate() throws IOException {
        InputStream templateStream = getClass().getResourceAsStream("/templates/template.pdf");
        RandomAccessRead rar = new RandomAccessReadBuffer(templateStream);
        return Loader.loadPDF(rar);
    }
    private String getBillNumber(Long billNumber){
        int times = 8 - String.valueOf(billNumber).length();
        return "0".repeat(times) + billNumber;
    }
    private String getCompanyInfo(Bill bill){
        StringBuilder sb = new StringBuilder();
        String email = bill.getCompanyEmail();
        String address = bill.getCompanyAddress();
        if (email != null && !email.isEmpty()) sb.append(email).append("\n");
        if (address != null && !address.isEmpty()) sb.append(address);
        return sb.toString();
    }
    private String getCustomerInfo(Bill bill) {
        StringBuilder sb = new StringBuilder();
        String email = bill.getCustomerEmail();
        String address = bill.getCustomerAddress();
        if (email != null && !email.isEmpty()) sb.append(email).append("\n");
        if (address != null && !address.isEmpty()) sb.append(address);
        return sb.toString();
    }
    private String dateFormatter(LocalDate date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(formatter);
    }
    private void saveBillLines(List<BillLine> billLines, PDAcroForm acroForm) throws IOException {
        for (int i = 0, x = 1; i < billLines.size(); i++, x++){
            BillLine line = billLines.get(i);
            if (line.getCode() != null) acroForm.getField("code" + x).setValue(line.getCode());
            acroForm.getField("productName" + x).setValue(line.getName());
            acroForm.getField("unitPrice" + x).setValue(formatAmount(line.getPrice()));
            acroForm.getField("quantity" + x).setValue(String.valueOf(line.getQuantity()));
            acroForm.getField("total" + x).setValue(formatAmount(line.getTotal()));
        }
    }
    private String savePdf(PDDocument template, Long companyId, String billNumber) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        template.save(baos);
        byte[] pdfBytes = baos.toByteArray();

        String path = "bills/" + companyId + "/";

        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String dateSt = date.format(formatter);
        UUID uuid = UUID.randomUUID();
        String uuidSt = uuid.toString();
        String node = uuidSt.substring(uuidSt.lastIndexOf("-") + 1);
        String name = billNumber + "_" + dateSt + "_" + node.toUpperCase();

        MultipartFile multipartFile = new ByteArrayMultipartFile(
                pdfBytes,
                name + ".pdf",
                name + ".pdf",
                "application/pdf"
        );

        return cloudinaryService.uploadField(multipartFile, path, name + ".pdf", true);
    }
    private String formatAmount(BigDecimal amount) {
        return String.format(Locale.US, "%.2f", amount);
    }
    private void setImageInAcroButton(PDDocument template, PDImageXObject image, PDAcroForm form, String field, boolean isPdf){
        PDPushButton button = (PDPushButton) form.getField(field);
        List<PDAnnotationWidget> widgets = button.getWidgets();

        if (widgets != null && !widgets.isEmpty()) {
            PDAnnotationWidget widget = widgets.getFirst();
            PDRectangle rect = widget.getRectangle();

            float height = rect.getHeight();
            float width = isPdf ? height / ((float) image.getHeight() / image.getWidth()) : rect.getWidth();

            PDAppearanceStream apStream = new PDAppearanceStream(template);
            PDResources rs = new PDResources();
            apStream.setResources(rs);
            rs.add(image);
            apStream.setBBox(new PDRectangle(0, 0, width, height));

            try (PDPageContentStream cs = new PDPageContentStream(template, apStream)) {
                cs.drawImage(image, 0, 0, width, height);
            } catch (IOException e) {
                throw new InternalServerException("Error al generar la factura");
            }

            PDAppearanceDictionary apDict = widget.getAppearance();
            if (apDict == null) {
                apDict = new PDAppearanceDictionary();
                widget.setAppearance(apDict);
            }
            apDict.setNormalAppearance(apStream);
        }

    }

}
