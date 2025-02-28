
import {
    completeGroupOrder,
    getGroupOrderById,
    getOrderById,
    payOrder,
    validateGroupOrder
} from "./utils/apiService.js";


const orderId = sessionStorage.getItem("orderId");
const deliveryAddressElement = document.getElementById("delivery-address");
const deliveryTimeElement = document.getElementById("delivery-time");
const itemsOrderedContainer = document.querySelector(".items-ordered");
const totalPriceElement = document.getElementById("total-price");

const payButton = document.querySelector(".pay");
const popup = document.getElementById("popup-payment");
const closePopupButton = document.getElementById("close-checkout");
const closeGroupOrderButton = document.getElementById("close-group-order");

async function loadCheckoutDetails() {
    try {
        if (!orderId) {
            throw new Error("No order ID found in sessionStorage.");
        }

        const orderDetails = await getOrderById(orderId);
        console.log("Order details:", orderDetails);
        document.getElementById("restaurant-name").textContent = orderDetails.restaurant.name;
        const deliveryAddress = orderDetails.deliveryLocation.address;
        deliveryAddressElement.textContent = `${deliveryAddress.street}, ${deliveryAddress.city}`;
        if (orderDetails.deliveryTime) {
            const deliveryTime = orderDetails.deliveryTime;
            deliveryTimeElement.textContent = `${deliveryTime[2]}/${deliveryTime[1]}/${deliveryTime[0]}, ${deliveryTime[3]}:${deliveryTime[4].toString().padStart(2, '0')}`;
        }
        itemsOrderedContainer.innerHTML = "";

        orderDetails.orderItems.forEach(item => {
            const itemRow = document.createElement("div");
            itemRow.classList.add("item-row");
            itemRow.innerHTML = `

                <label><strong>${item.item.name}</strong> : ${item.item.price} € x ${item.quantity}</label>
                <span>Total: ${item.totalPrice} €</span>
            `;

            itemsOrderedContainer.appendChild(itemRow);
        });

        totalPriceElement.value = `${orderDetails.totalAmount} €`;

    } catch (error) {
        console.error("Error loading checkout details:", error);
        itemsOrderedContainer.innerHTML = "<p>Failed to load items. Please try again later.</p>";
        alert("Error: Unable to load checkout details. Please try again.");
    }
}

payButton.addEventListener("click", async () => {
    try {
        await payOrder(orderId);
        console.log("Order paid successfully.");
        popup.style.display = "block";
        setTimeout(() => {
            window.location.href = "paymentConfirmation.html";
        }, 3000);
    } catch (error) {
        console.error("Error paying order:", error);
        alert("Error: Unable to process payment. Please try again.");
    }

});

closePopupButton.addEventListener("click", () => {
    popup.style.display = "none";

});

if (sessionStorage.getItem("groupOrderId")) {
    closeGroupOrderButton.style.display = "block";
}

closeGroupOrderButton.addEventListener("click", async () => {
    try {
        const groupOrderId = sessionStorage.getItem("groupOrderId");
        const group = await getGroupOrderById(groupOrderId);

        let groupOrderData = {
            orderID: sessionStorage.getItem("orderId"),
        };

        if (!group.deliveryTime) {
            groupOrderData.deliveryTime = "2024-12-31T14:00:00";
            groupOrderData.possibleTime = "2024-12-31T14:00:00";
        }

        groupOrderData = JSON.stringify(groupOrderData);

        await validateGroupOrder(groupOrderData);
        closeGroupOrderButton.textContent = "Group order closed";
        closeGroupOrderButton.disabled = true;
        closeGroupOrderButton.style.cursor = "not-allowed";

        console.log("Group order validated successfully.");
    } catch (error) {
        console.error("Error validating group order:", error);
        alert("Error: Unable to close group order. Please try again.");
    }
});

document.addEventListener("DOMContentLoaded", async () => {
    try {
        await loadCheckoutDetails();
        console.log("Checkout details loaded successfully.");
    } catch (error) {
        console.error("Error loading checkout details:", error);
    }

});

if (sessionStorage.getItem("groupOrderId")) {
    document.getElementById("close-group-order").style.display = "block";
}

const closeGroupOrderButton = document.getElementById("close-group-order");
closeGroupOrderButton.addEventListener("click", async () => {
    const groupOrderId = sessionStorage.getItem("groupOrderId");
    const group = await getGroupOrderById(groupOrderId);
    let groupOrderData = {
        orderID: sessionStorage.getItem("orderId"),
    }
    if (!group.deliveryTime){
        groupOrderData = {
            orderID: sessionStorage.getItem("orderId"),
            deliveryTime: "2024-12-31T14:00:00",
            possibleTime:  "2024-12-31T14:00:00",
        }
    }

    groupOrderData = JSON.stringify(groupOrderData);
    try {
        validateGroupOrder(groupOrderData).then(() => {
            closeGroupOrderButton.textContent = "Group order closed";
            closeGroupOrderButton.disabled = true;
            closeGroupOrderButton.style.cursor = "not-allowed";
        });
    } catch (error) {
        console.error("Error validating group order:", error);
        alert("Failed to close group order. Please try again later.");
    }

});


loadCheckoutDetails().then(r => console.log("Checkout details loaded."));
