import {
    addItemToOrder,

    completeGroupOrder,
    getAllMenus,

    getMenusByRestaurant,
    getRestaurantById,
    placeOrder
} from "./utils/apiService.js";

export async function displayItems(restaurantId, deliveryDate) {
    const itemsContainer = document.getElementById('items-container');
    const restaurantNameHeader = document.getElementById('restaurant-name');

    try {
        let items;


        if (deliveryDate && deliveryDate !== 'undefined') {
            items = await getMenusByRestaurant(restaurantId, deliveryDate);
        } else {
            items = await getAllMenus(restaurantId);
        }

        const restaurant = await getRestaurantById(restaurantId);
        restaurantNameHeader.textContent = restaurant.name;

        itemsContainer.innerHTML = '';
        items.forEach(item => {

            const itemCard = document.createElement('div');
            itemCard.classList.add('item-card');

            itemCard.innerHTML = `
                <h2>${item.name}</h2>
                <div class="item-card-details">
                    <p>Price: ${item.price} € - ${item.preparationTimeInMinutes} min</p>
                    <div>
                        <label for="quantity-${item.id}">Quantity : </label>
                        <input id="quantity-${item.id}" class="quantity" min="0" type="number" value="0">
                    </div>
                    <button class="add-item" data-id="${item.id}" data-price="${item.price}">Add</button>
                </div>
            `;

            itemsContainer.appendChild(itemCard);
        });


        console.log('Items displayed successfully.');

        attachAddItemListeners();
    } catch (error) {
        console.error('Error fetching menu items:', error);
        itemsContainer.innerHTML = '<p>Failed to load items. Please try again later.</p>';

        alert('Error: Unable to load menu items. Please try again later.');

    }
}

const cartItems = {};

function attachAddItemListeners() {
    const addButtons = document.querySelectorAll('.add-item');
    const cartCount = document.getElementById('cart-count');
    const totalPrice = document.getElementById('total-price');

    let cartItemCount = 0;
    let cartTotalPrice = 0;

    addButtons.forEach(button => {

        button.addEventListener('click', async () => {

            const itemId = button.getAttribute('data-id');
            const itemPrice = parseFloat(button.getAttribute('data-price'));
            const quantityInput = document.getElementById(`quantity-${itemId}`);
            const quantity = parseInt(quantityInput.value);

            if (quantity > 0) {
                if (cartItems[itemId]) {
                    const previousQuantity = cartItems[itemId].quantity;
                    const difference = quantity - previousQuantity;

                    cartItems[itemId].quantity = quantity;

                    cartItemCount += difference;
                    cartTotalPrice += difference * itemPrice;
                } else {
                    cartItems[itemId] = { menuItemId: itemId, quantity: quantity };

                    cartItemCount += quantity;
                    cartTotalPrice += quantity * itemPrice;
                }

                const orderItemId = sessionStorage.getItem('orderId');

                try {
                    await addItemToOrder(orderItemId, JSON.stringify(cartItems[itemId]));
                    console.log(`Item added to order successfully.`);
                } catch (error) {
                    console.error('Error adding item to order:', error);
                    alert('Error: Failed to add item to the order.');
                }

                cartCount.textContent = cartItemCount;
                totalPrice.textContent = `${cartTotalPrice.toFixed(2)} €`;
                console.log('Cart updated:', cartItems);
            } else {
                alert('Please enter a valid quantity.');
            }
        });
    });
}

const restaurantId = sessionStorage.getItem('selectedRestaurantId');
const deliveryDate = sessionStorage.getItem('deliveryTime');


document.addEventListener('DOMContentLoaded', async () => {
    try {
        await displayItems(restaurantId, deliveryDate);
    } catch (error) {
        console.error('Error displaying items:', error);
        alert('Error: Unable to load the page. Please try again later.');
    }
});

const checkoutButton = document.querySelector('.checkout-button');
const orderId = sessionStorage.getItem('orderId');

if (checkoutButton) {

    checkoutButton.addEventListener('click', async () => {
        try {
            await placeOrder(orderId);
            console.log('Order placed successfully.');

            try {
                await completeGroupOrder(orderId);
                console.log('Group order completed successfully.');
                alert('Order placed and group completed successfully!');
                window.location.href = 'checkoutSummary.html';
            } catch (error) {
                console.error('Error completing group order:', error);
                alert('Error: Failed to complete the group order.');
            }
        } catch (error) {
            console.error('Error placing order:', error);
            alert('Error: Unable to place the order. Please try again.');
        }
    });
}
