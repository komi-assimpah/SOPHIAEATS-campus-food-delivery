
import {getGroupOrderById, getRestaurants} from "./utils/apiService.js";

import {
    displayRestaurantCards,
    closePopup,
    fillDeliveryAddresses,
    fillDeliveryTime,

    confirmDeliveryDetails,
    fillAllDeliveryTimes,
    createGroupDetails,
    joinGroupDetails
} from "./utils/utilFunctions.js";

const popupJoinGroup = document.getElementById('popup-join-group');

const popupCreateGroup = document.getElementById('popup-create-group');
const identifierPopup = document.getElementById("popup-group-identifier");
const popupDelivery = document.getElementById('popup-index-restaurant');

function closeAllPopups() {
    popupJoinGroup.style.display = 'none';
    popupCreateGroup.style.display = 'none';

    identifierPopup.style.display = 'none';
    popupDelivery.style.display = 'none';
}

document.getElementById('join-group-order').addEventListener('click', () => {

    closeAllPopups();
    popupJoinGroup.style.display = 'block';
});


document.getElementById('create-group-order').addEventListener('click', () => {

    closeAllPopups();
    popupCreateGroup.style.display = 'block';
});


document.getElementById('close-join-group').addEventListener('click', () => {
    popupJoinGroup.style.display = 'none';
});

document.getElementById('close-create-group').addEventListener('click', () => {
    popupCreateGroup.style.display = 'none';
});

document.getElementById("close-group-identifier").addEventListener("click", () => {
    identifierPopup.style.display = "none";
});

document.getElementById('close-index-restaurant').addEventListener('click', () => {
    closePopup(popupDelivery);
});

document.getElementById('group-joining').addEventListener('click', async () => {
    const groupID = document.getElementById('group-identifier').value;

    try {
        const group = await getGroupOrderById(groupID);
        sessionStorage.setItem('groupOrderId', group.groupID);
        await joinGroupDetails(group.deliveryLocationID, group.deliveryTime, groupID);
        alert('Vous avez rejoint le groupe avec succès !');
        sessionStorage.setItem('groupJoined', 'true');
        window.location.href = 'restaurantList.html';
    } catch (error) {
        console.error('Error joining group:', error);
        alert('Échec : impossible de rejoindre le groupe. Vérifiez l’identifiant ou si le groupe est fermé.');
    }
});

document.getElementById('group-creating').addEventListener('click', async () => {
    const address = document.querySelector('.delivery-address-group').value;
    const time = document.querySelector('.all-delivery-time').value;

    try {
        const groupOrder = await createGroupDetails(address, time);
        sessionStorage.setItem('groupOrderId', groupOrder.groupID);

        setTimeout(() => {
            console.log('Group created:', groupOrder);
            popupCreateGroup.style.display = 'none';
            identifierPopup.style.display = "block";
            document.getElementById('your-identifier').value = groupOrder.groupID;
            alert('Groupe créé avec succès ! Votre identifiant est : ' + groupOrder.groupID);
        }, 1000);
    } catch (error) {
        console.error('Error creating group:', error);
        alert('Échec : impossible de créer le groupe.');
    }
});

document.getElementById("confirm-identifier").addEventListener("click", () => {
    identifierPopup.style.display = "none";
    window.location.href = 'restaurantList.html';
});

const restaurantList = document.getElementById('restaurant-list');

async function fetchAndDisplayRestaurants() {
    try {
        const restaurants = await getRestaurants();
        displayRestaurantCards(restaurants, restaurantList, popupDelivery);
    } catch (error) {
        console.error('Error fetching restaurants:', error);
        alert('Erreur : impossible de charger la liste des restaurants.');
    }
}

document.addEventListener('DOMContentLoaded', async () => {
    try {
        await fetchAndDisplayRestaurants();

        await fillDeliveryAddresses(document.querySelector('.delivery-address'));
        console.log('Delivery addresses filled.');

        await fillDeliveryAddresses(document.querySelector('.delivery-address-group'));
        console.log('Delivery addresses filled for group.');

        const restaurantId = sessionStorage.getItem('selectedRestaurantId');
        if (restaurantId) {
            await fillDeliveryTime(restaurantId);
            console.log('Delivery times filled.');
        }

        if (document.querySelector('.all-delivery-time')) {
            await fillAllDeliveryTimes();
            console.log('All delivery times filled.');
        }
    } catch (error) {
        console.error('Error during page initialization:', error);
        alert('Erreur : impossible de charger les données initiales.');
    }
});

document.getElementById('confirm-delivery').addEventListener('click', async () => {
    const addressSelect = document.querySelector('.delivery-address').value;
    const timeSelect = document.querySelector('.delivery-time').value;

    try {
        const order = await confirmDeliveryDetails(addressSelect, timeSelect);
        sessionStorage.setItem('orderId', order.id);

        setTimeout(() => {
            console.log('Order:', order);
            alert('Commande confirmée avec succès !');
            window.location.href = 'orderPage.html';
        }, 1000);
    } catch (error) {
        console.error('Error confirming order:', error);
        alert('Échec : impossible de confirmer la commande.');
    }
});

