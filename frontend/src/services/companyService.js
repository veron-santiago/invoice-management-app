const BASE_URL = 'http://localhost:8080';

export const hasAccessToken = async (token) => {
    const response = await fetch(`${BASE_URL}/companies/access-token`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });
    if (!response.ok) {
        throw new Error('Error checking access token');
    }
    return await response.json();
};

export const getMercadoPagoConnection = async (token) => {
    const response = await fetch(`${BASE_URL}/mp/connect`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });
    if (!response.ok) {
        throw new Error('Error getting Mercado Pago connection URL');
    }
    return await response.text();
};
