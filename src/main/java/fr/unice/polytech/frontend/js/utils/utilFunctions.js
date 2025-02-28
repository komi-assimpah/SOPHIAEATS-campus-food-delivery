import {
    getDeliveryAddresses,
    getAvailableDeliveryTimes,
    createOrder,
    getAvailableTimes, createGroupOrder, joinGroupOrder, chooseRestaurant
} from './apiService.js';

export function displayRestaurantCards(restaurantsData, restaurantList, popup) {
    restaurantList.innerHTML = '';

    restaurantsData.forEach(restaurant => {
        const restaurantCard = document.createElement('div');
        restaurantCard.classList.add('restaurant-card');
        restaurantCard.setAttribute('data-id', restaurant.id);

        const isOpen = isRestaurantOpen(restaurant);

        restaurantCard.innerHTML = `
            <h2>${restaurant.name}</h2>
            <div class="restaurant-card-details">
                <p>${restaurant.address.street}, ${restaurant.address.city}</p>
                <p>${isOpen ? 'Open' : 'Closed'}</p>
            </div>
        `;

        restaurantCard.addEventListener('click', async function () {
            const restaurantId = restaurantCard.getAttribute("data-id");
            const groupOrder = sessionStorage.getItem('groupOrderId');
            const joinedGroup = sessionStorage.getItem('groupJoined');
            if (!groupOrder && !joinedGroup) {
                sessionStorage.setItem('selectedRestaurantId', restaurantId);
                popup.style.display = 'block';
                restaurantCard.classList.add('selected');
            } else {
                restaurantCard.classList.add('selected');
                //const userId = sessionStorage.getItem('userId');
                const userId = "2";

                let restaurantData = {
                    restaurantId: restaurantId,
                }
                restaurantData = JSON.stringify(restaurantData);
                await chooseRestaurant(userId, restaurantData).then(() => {
                    console.log('Restaurant chosen');
                    sessionStorage.setItem('selectedRestaurantId', restaurantId);
                    window.location.href = 'orderPage.html';
                });


            }
        });

        restaurantList.appendChild(restaurantCard);
    });
}

function isRestaurantOpen(restaurant) {
    return restaurant.schedules.some(schedule => {
        // For testing purposes when the restaurant is open
        // const today = new Date("2024-12-30T13:00:00");
        const today = new Date();
        const daysOfWeek = [
            "SUNDAY",
            "MONDAY",
            "TUESDAY",
            "WEDNESDAY",
            "THURSDAY",
            "FRIDAY",
            "SATURDAY"
        ];
        const day = daysOfWeek[today.getDay()];
        const hours = today.getHours();
        const minutes = today.getMinutes();
        const currentTime = hours * 60 + minutes;

        const openingDay = schedule.day;
        const openingTimeHour = schedule.startTime[0];
        const openingTimeMinute = schedule.startTime[1];
        const closingTimeHour = schedule.endTime[0];
        const closingTimeMinute = schedule.endTime[1];
        const openingTime = openingTimeHour * 60 + openingTimeMinute;
        const closingTime = closingTimeHour * 60 + closingTimeMinute;

        return openingDay === day && currentTime >= openingTime && currentTime <= closingTime;
    });
}

export function closePopup(popup) {
    popup.style.display = 'none';
    document.querySelectorAll('.restaurant-card.selected').forEach(card => {
        card.classList.remove('selected');
    });
}

export async function fillDeliveryAddresses(selectElement) {
    try {
        const addresses = await getDeliveryAddresses();
        console.log('addresses:', addresses);
        addresses.forEach(address => {
            const option = document.createElement('option');
            option.value = address.id;
            console.log('address:', address.id);
            option.text = `${address.name}, ${address.address.street}`;
            selectElement.add(option);
        });
    } catch (error) {
        console.error('Error fetching delivery addresses:', error);
    }
}

export async function fillDeliveryTime(restaurantId) {
    const selectElement = document.querySelector('.delivery-time');

    try {
        const times = await getAvailableDeliveryTimes(restaurantId);

        times.forEach(time => {
            const date = time[0];
            const month = time[1];
            const day = time[2];
            const hours = time[3];
            const minutes = time[4];

            const formattedTime = `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`;
            const valueTime = `${date}-${month}-${day}T${formattedTime}`;

            const option = document.createElement('option');
            option.value = valueTime;
            option.text = formattedTime;

            selectElement.add(option);
        });
    } catch (error) {
        console.error('Error fetching delivery times:', error);
    }
}

export async function fillAllDeliveryTimes() {
    const selectElement = document.querySelector('.all-delivery-time');

    try {
        const deliveryTimes = await getAvailableTimes();

        deliveryTimes.sort((a, b) => {
            const dateA = new Date(a[0], a[1] - 1, a[2], a[3], a[4]);
            const dateB = new Date(b[0], b[1] - 1, b[2], b[3], b[4]);
            return dateA - dateB;
        });

        deliveryTimes.forEach(time => {
            const date = time[0];
            const month = time[1];
            const day = time[2];
            const hours = time[3];
            const minutes = time[4];

            const formattedTime = `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`;
            const valueTime = `${date}-${month}-${day}T${formattedTime}`;

            const option = document.createElement('option');
            option.value = valueTime;
            option.text = formattedTime;

            selectElement.add(option);
        });
    } catch (error) {
        console.error('Error fetching delivery times:', error);
    }
}

export async function confirmDeliveryDetails(addressSelect, timeSelect) {
    const deliveryLocationId = addressSelect;
    const deliveryTime = timeSelect;

    sessionStorage.setItem('deliveryAddressId', deliveryLocationId);
    sessionStorage.setItem('deliveryTime', deliveryTime);

    try {
        const restaurantId = sessionStorage.getItem('selectedRestaurantId');
        //const userId = sessionStorage.getItem('userId');
        const userId = "2";

        sessionStorage.setItem('userId', userId);
        let orderData = {
            restaurantId: restaurantId,
            userId: userId,
            deliveryLocationId: deliveryLocationId,
            deliveryTime: deliveryTime,
        };
        orderData = JSON.stringify(orderData);
        const order = await createOrder(orderData);
        console.log('Order created:', order);
        return order;
    } catch (error) {
        console.error('Error creating order:', error);
    }

}

export async function createGroupDetails(addressSelect, timeSelect) {
    const deliveryLocationId = addressSelect;
    const deliveryTime = timeSelect;

    sessionStorage.setItem('deliveryAddressId', deliveryLocationId);
    sessionStorage.setItem('deliveryTime', deliveryTime);

    try {
        //const userId = sessionStorage.getItem('userId');
        const userId = "2";

        sessionStorage.setItem('userId', userId);

        let orderData = {
            userId: userId,
            deliveryLocationId: deliveryLocationId,
        };

        if (deliveryTime) {
            orderData.deliveryTime = deliveryTime;
        }

        orderData = JSON.stringify(orderData);
        const order = await createOrder(orderData);

        let groupOrderData = {
            orderID: order.id,
            locationID: deliveryLocationId,
        };

        if (deliveryTime) {
            groupOrderData.deliveryTime = deliveryTime;
        }

        groupOrderData = JSON.stringify(groupOrderData);
        const groupOrder = await createGroupOrder(groupOrderData);
        sessionStorage.setItem('orderId', order.id);
        console.log('Order created:', groupOrder);
        return groupOrder;
    } catch (error) {
        console.error('Error creating order:', error);
    }
}

export async function joinGroupDetails(deliveryLocationId, deliveryTime, groupID) {
    sessionStorage.setItem('deliveryAddressId', deliveryLocationId);
    if (deliveryTime && deliveryTime !== 'null') {
        const [year, month, day, hours, minutes] = [deliveryTime[0], deliveryTime[1], deliveryTime[2], deliveryTime[3], deliveryTime[4]];
        const formattedTime = `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`;
        const parsedDeliveryTime = `${year}-${month}-${day}T${formattedTime}`;
        sessionStorage.setItem('deliveryTime', parsedDeliveryTime);
    }

    try {
        //const userId = sessionStorage.getItem('userId');
        const userId = "2";

        sessionStorage.setItem('userId', userId);

        let orderData = {
            userId: userId,
            deliveryLocationId: deliveryLocationId,
        };

        if (deliveryTime && deliveryTime !== 'null') {
            const [year, month, day, hours, minutes] = [deliveryTime[0], deliveryTime[1], deliveryTime[2], deliveryTime[3], deliveryTime[4]];
            const formattedTime = `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`;
            const parsedDeliveryTime = `${year}-${month}-${day}T${formattedTime}`;
            orderData.deliveryTime = parsedDeliveryTime;
        }

        orderData = JSON.stringify(orderData);
        const order = await createOrder(orderData);
        sessionStorage.setItem('orderId', order.id);

        let groupOrderData = {
            orderID: order.id,
        };

        groupOrderData = JSON.stringify(groupOrderData);
        const message = await joinGroupOrder(groupOrderData, groupID);
        console.log('Group Order joined:', message);
    } catch (error) {
        console.error('Error creating order:', error);
    }

}