const API_GATEWAY_URL = "http://localhost:8080/api";

/**
 * Effectue une requête HTTP générique.
 * @param {string} method - Méthode HTTP: GET, POST, PUT, DELETE.
 * @param {Object} [body] - Les données à envoyer dans la requête.
 * @param {string} api_url - L'URL de l'API.
 * @returns {Promise<Object>} - La réponse JSON du serveur.
 */
async function httpRequest(api_url, method = "GET", body = null) {
    const headers = {
        "Content-Type": "application/json",
    };

    const options = {
        method,
        headers,
    };

    if (body) {
        options.body = JSON.stringify(body);
    }

    try {
        const response = await fetch(`${api_url}`, options);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return await response.json();
    } catch (error) {
        console.error(`Error with ${method} request to ${api_url}:`, error);
        throw error;
    }
}

/**
 * Récupère tous les restaurants.
 */
export function getRestaurants() {
    return httpRequest(`${API_GATEWAY_URL}/restaurants`);
}

/**
 * Récupère tous les commandes d'un utilisateur.
 * @param {string} userId - L'ID de l'utilisateur.
 */
export function getOrderById(orderId) {
    return httpRequest(`${API_GATEWAY_URL}/orders/${orderId}`);
}

/**
 * Crée une nouvelle commande.
 * @param {Object} orderData - Les données de la commande.
 */
export async function createOrder(orderData) {
    try{
        const response = await fetch(`${API_GATEWAY_URL}/orders`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: orderData,
        });
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return await response.json();
    }
    catch(error){
        console.error('Error creating order:', error);
        throw error;
    }
}


/**
 * Ajoute un item à une commande existante.
 * @param {string} orderId - L'ID de la commande.
 * @param {Object} itemData - Les données de l'item (menuItemId, quantity).
 */
export async function addItemToOrder(orderId, itemData) {
    try{
        const response = await fetch(`${API_GATEWAY_URL}/orders/${orderId}/items`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
            },
            body: itemData,
        });
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return await response.json();
    }
    catch(error){
        console.error('Error adding item to order:', error);
        throw error;
    }
}

export async function chooseRestaurant(userId, restaurantData) {
    try{
        const response = await fetch(`${API_GATEWAY_URL}/orders/${userId}/chooseRestaurant`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
            },
            body: restaurantData,
        });
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return await response.json();
    }
    catch(error){
        console.error('Error adding item to order:', error);
        throw error;
    }
}

/**
 * Place une commande existante.
 * @param {string} orderId - L'ID de la commande.
 */
export function placeOrder(orderId) {
    return httpRequest(`${API_GATEWAY_URL}/orders/${orderId}/placement`);
}

/**
 * Paye une commande existante.
 * @param {string} orderId - L'ID de la commande.
 */
export function payOrder(orderId) {
    return httpRequest(`${API_GATEWAY_URL}/orders/${orderId}/payment`);
}

/**
 * Récupère les adresses de livraison d'un utilisateur.
 */
export function getDeliveryAddresses() {
    return httpRequest(`${API_GATEWAY_URL}/locations`);
}

export function getDeliveryAddressById(locationId) {
    return httpRequest(`${API_GATEWAY_URL}/locations/${locationId}`);
}

export function getRestaurantById(restaurantId) {
    return httpRequest(`${API_GATEWAY_URL}/restaurants/${restaurantId}`);
}

export function getMenusByRestaurant(restaurantId, deliveryDate) {
    return httpRequest(`${API_GATEWAY_URL}/orders/${restaurantId}/items?deliveryDate=${deliveryDate}`);
}

export function getAllMenus(restaurantId) {
    return httpRequest(`${API_GATEWAY_URL}/orders/${restaurantId}/menu`);
}

export function getAvailableDeliveryTimes(restaurantId) {
    return httpRequest(`${API_GATEWAY_URL}/orders/${restaurantId}/times`);
}

export function getAvailableTimes() {
    return httpRequest(`${API_GATEWAY_URL}/restaurants/times`);
}

export async function createGroupOrder(groupOrderData) {
    try {
        const response = await fetch(`${API_GATEWAY_URL}/groupOrders/create`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: groupOrderData,
        });
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return await response.json();
    } catch (error) {
        console.error('Error creating order:', error);
        throw error;
    }
}

export function getGroupOrderById(groupOrderId) {
    return httpRequest(`${API_GATEWAY_URL}/groupOrders/group/${groupOrderId}`);
}

export async function joinGroupOrder(groupOrderData, groupOrderId) {
    try {
        const response = await fetch(`${API_GATEWAY_URL}/groupOrders/join/${groupOrderId}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
            },
            body: groupOrderData,
        });
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return await response.json();
    } catch (error) {
        console.error('Error adding item to order:', error);
        throw error;
    }
}

export async function completeGroupOrder(orderId) {
    try {
        const response = await fetch(`${API_GATEWAY_URL}/groupOrders/complete?orderID=${orderId}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
            },
        });
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return await response.json();
    } catch (error) {
        console.error('Error adding item to order:', error);
        throw error;
    }
}

export async function validateGroupOrder(groupOrderData) {
    try {
        const response = await fetch(`${API_GATEWAY_URL}/groupOrders/validate`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
            },
            body: groupOrderData
        });
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return await response.json();
    } catch (error) {
        console.error('Error adding item to order:', error);
        throw error;
    }
}

export async function confirmGroupOrder(orderId) {
    try {
        const response = await fetch(`${API_GATEWAY_URL}/groupOrders/confirm?orderID=${orderId}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
            },
        });
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return await response.json();
    } catch (error) {
        console.error('Error adding item to order:', error);
        throw error;
    }
}

export async function createAccount(userData) {
    try {
        const response = await fetch(`${API_GATEWAY_URL}/users`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(userData),
        });

        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error('Error creating user:', error);
        throw error;
    }
}

export async function loginAccount(userData) {
    try {
        const response = await fetch(`${API_GATEWAY_URL}/users/login`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: userData,
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || `HTTP error! status: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error("Erreur lors de la tentative de connexion :", error);
        throw error;
    }
}

export function getUser(userId) {
    return httpRequest(`${API_GATEWAY_URL}/users/${userId}`);
}
