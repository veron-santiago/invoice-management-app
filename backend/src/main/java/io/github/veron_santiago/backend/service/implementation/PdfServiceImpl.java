package io.github.veron_santiago.backend.service.implementation;

import io.github.veron_santiago.backend.persistence.entity.Bill;
import io.github.veron_santiago.backend.persistence.entity.BillLine;
import io.github.veron_santiago.backend.persistence.repository.IBillRepository;
import io.github.veron_santiago.backend.service.exception.ErrorMessages;
import io.github.veron_santiago.backend.service.exception.ObjectNotFoundException;
import io.github.veron_santiago.backend.service.interfaces.IPdfService;
import io.github.veron_santiago.backend.util.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PdfServiceImpl implements IPdfService {

    private final IBillRepository billRepository;
    private final AuthUtil authUtil;

    public PdfServiceImpl(IBillRepository billRepository, AuthUtil authUtil) {
        this.billRepository = billRepository;
        this.authUtil = authUtil;
    }

    @Override
    public String generateBillPdf(Bill bill) throws IOException {
        try(PDDocument template = getTemplate()){
            
            PDDocumentCatalog catalog = template.getDocumentCatalog();
            PDAcroForm form = catalog.getAcroForm();
            if (form == null) throw new IllegalStateException(ErrorMessages.PDF_GENERATE_ERROR.getMessage());

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

            form.getField("companyNameTittle").setValue(bill.getCompanyName());
            form.getField("billNumber").setValue(billNumber);
            form.getField("issue_af_date").setValue(dateFormatter(bill.getIssueDate()));
            form.getField("due_af_date").setValue(dateFormatter(bill.getDueDate()));
            form.getField("companyName").setValue(bill.getCompanyName());
            form.getField("customerName").setValue(bill.getCustomerName());
            form.getField("companyInfo").setValue(getCompanyInfo(bill));
            form.getField("customerInfo").setValue(getCustomerInfo(bill));
            form.getField("totalAmount").setValue("$ " + formatAmount(bill.getTotalAmount()));
            saveBillLines(bill.getBillLines(), form);
            form.flatten();

            return savePdf(template, companyId, billNumber);

        }
    }

    @Override
    public byte[] getPdfByBillId(Long billId, HttpServletRequest request) throws AccessDeniedException {

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ObjectNotFoundException(ErrorMessages.BILL_NOT_FOUND.getMessage()));
        Long companyId = bill.getCompany().getId();
        if (!Objects.equals(companyId, authUtil.getAuthenticatedCompanyId(request))) {
            throw new AccessDeniedException(ErrorMessages.ACCESS_DENIED_READ.getMessage());
        }

        String billNUmber = getBillNumber(bill.getBillNumber());
        Path path = getStoragePath(companyId).resolve(billNUmber + ".pdf");
        if (Files.notExists(path)) {
            throw new ObjectNotFoundException(ErrorMessages.BILL_NOT_FOUND.getMessage());
        }

        try{
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new IllegalStateException(ErrorMessages.PDF_GENERATE_ERROR.getMessage());
        }
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
    private String getCustomerInfo(Bill bill){
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
        Path storagePath = getStoragePath(companyId);
        Files.createDirectories(storagePath);

        String fileName = billNumber + ".pdf";
        Path outputPath = storagePath.resolve(fileName);
        template.save(outputPath.toFile());
        return outputPath.toString();
    }
    private Path getStoragePath(Long companyId){
        return Paths.get(System.getProperty("user.dir"))
                .getParent()
                .resolve("storage")
                .resolve("bills")
                .resolve(companyId.toString());
    };
    private String formatAmount(BigDecimal amount) {
        return String.format(Locale.US, "%.2f", amount);
    }


}
