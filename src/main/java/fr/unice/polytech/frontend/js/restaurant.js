import { getRestaurants } from './utils/apiService.js';
import {
    displayRestaurantCards,
    closePopup,
    fillDeliveryAddresses,
    fillDeliveryTime,
    confirmDeliveryDetails

} from './utils/utilFunctions.js';

const restaurantList = document.getElementById('restaurant-list');
const popup = document.getElementById('popup-restaurant');
const close = document.getElementById('close-restaurant');
const confirmButton = document.getElementById('confirm-delivery');
const groupButton = document.getElementById('group-joined');

async function fetchAndDisplayRestaurants() {
    try {
        const restaurants = await getRestaurants();
        displayRestaurantCards(restaurants, restaurantList, popup);

        console.log('Restaurants chargés et affichés avec succès.');
    } catch (error) {
        console.error('Erreur lors du chargement des restaurants:', error);
        alert('Erreur : impossible de charger les restaurants. Veuillez réessayer plus tard.');
    }
}

async function loadDeliveryAddresses() {
    try {
        await fillDeliveryAddresses(document.querySelector('.delivery-address'));
        console.log('Adresses de livraison remplies avec succès.');
    } catch (error) {
        console.error('Erreur lors du chargement des adresses:', error);
        alert('Erreur : impossible de charger les adresses de livraison.');
    }
}

async function loadDeliveryTimes() {
    const restaurantId = sessionStorage.getItem('selectedRestaurantId');
    if (restaurantId) {
        try {
            await fillDeliveryTime(restaurantId);
            console.log('Horaires de livraison remplis avec succès.');
        } catch (error) {
            console.error('Erreur lors du chargement des horaires:', error);
            alert('Erreur : impossible de charger les horaires de livraison.');
        }
    }
}

confirmButton.addEventListener('click', async function () {
    const addressSelect = document.querySelector('.delivery-address').value;
    const timeSelect = document.querySelector('.delivery-time').value;

    try {
        const order = await confirmDeliveryDetails(addressSelect, timeSelect);
        sessionStorage.setItem('orderId', order.id);
        console.log('Commande confirmée avec succès:', order);
        alert('Commande confirmée avec succès !');
        setTimeout(() => {
            window.location.href = 'orderPage.html';
        }, 1000);
    } catch (error) {
        console.error('Erreur lors de la confirmation de la commande:', error);
        alert('Erreur : impossible de confirmer la commande. Veuillez réessayer.');
    }
});

if (sessionStorage.getItem('groupJoined') === 'true') {
    groupButton.style.display = 'block';
}

close.addEventListener('click', function () {
    closePopup(popup);
});

document.addEventListener('DOMContentLoaded', async () => {
    try {
        await fetchAndDisplayRestaurants();
        await loadDeliveryAddresses();
        await loadDeliveryTimes();
    } catch (error) {
        console.error('Erreur lors de l’initialisation de la page:', error);
        alert('Erreur : impossible de charger les données initiales. Veuillez réessayer plus tard.');
    }
});
