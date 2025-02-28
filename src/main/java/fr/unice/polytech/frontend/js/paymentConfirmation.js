
import {
    confirmGroupOrder,
    getDeliveryAddressById,
    getGroupOrderById,
    getOrderById,
    getUser
} from "./utils/apiService.js";

const ORDER_ID = sessionStorage.getItem("orderId");
const groupId = sessionStorage.getItem("groupOrderId");
const deliveryAddressElement = document.getElementById("delivery-address-group");
const deliveryTimeElement = document.getElementById("delivery-time-group");
const subOrder = document.getElementById("group-subOrder");

async function populateConfirmationPage(orderDetails) {
    const totalAmount = orderDetails.totalAmount;
    document.getElementById("totalAmount").value = `${totalAmount} €`;
    //const userId = sessionStorage.getItem("userId");
    const userId = "2";
    //const user = await getUser(userId);
    if (userId === orderDetails.user.id) {
        const userBalance = orderDetails.user.balance.toFixed(2);
        document.getElementById("balance-amount").value = `${userBalance} €`;

        const oldBalance = sessionStorage.getItem("balance");
        const balanceDifference = userBalance - oldBalance;
        sessionStorage.setItem("balance", userBalance);

        const balanceMessage = balanceDifference < 0 || balanceDifference === 0
            ? "Your balance hasn't changed."
            : "Your balance has increased by " + Math.abs(balanceDifference) + "€.";
        alert(balanceMessage);
    }
    const restaurantName = orderDetails.restaurant.name;
    document.getElementById("restaurant-name").textContent = restaurantName;

    const orderStatus = orderDetails.status;
    const orderStatusElement = document.getElementById("order-status");

    switch (orderStatus) {
        case "PENDING":
            orderStatusElement.textContent = "Your order is being processed.";
            orderStatusElement.style.color = "orange";
            break;
        case "CONFIRMED":
            orderStatusElement.textContent = "Your order has been confirmed.";
            orderStatusElement.style.color = "green";
            break;
        case "CANCELED":
            orderStatusElement.textContent = "Your order has been canceled.";
            orderStatusElement.style.color = "red";
            break;
        default:
            orderStatusElement.textContent = "Status unknown.";
            orderStatusElement.style.color = "gray";
            break;
    }
}

async function loadGroupOrderSummary() {
    try {
        if (!groupId) {
            throw new Error("No group order ID found in sessionStorage.");
        }

        const groupOrderDetails = await getGroupOrderById(groupId);
        console.log("Group Order details:", groupOrderDetails);

        const deliveryLocationId = groupOrderDetails.deliveryLocationID;
        const deliveryAddress = await getDeliveryAddressById(deliveryLocationId);
        console.log("Delivery address:", deliveryAddress);

        if (groupOrderDetails.deliveryTime) {
            const [year, month, day, hour, minute] = groupOrderDetails.deliveryTime;
            deliveryTimeElement.textContent = `${day}/${month}/${year}, ${hour}:${minute.toString().padStart(2, '0')}`;
        } else {
            deliveryTimeElement.textContent = "No delivery time available.";
        }
        deliveryAddressElement.textContent = `${deliveryAddress.address.street}, ${deliveryAddress.address.city}`;

        const subOrderIDs = groupOrderDetails.subOrderIDs;
        subOrder.innerHTML = "";

        if (subOrderIDs.length === 0) {
            subOrder.innerHTML = "<p>No sub-orders found.</p>";
            return;
        }

        for (const orderId of subOrderIDs) {
            try {
                const orderDetails = await getOrderById(orderId);
                console.log("Order details:", orderDetails);

                const subOrderItem = document.createElement("div");
                subOrderItem.classList.add("suborder-item");

                subOrderItem.innerHTML = `
                    <p><strong>Restaurant</strong>: ${orderDetails.restaurant.name}</p>
                    <div class="items-ordered-group"></div>
                `;

                const itemsOrderedContainer = subOrderItem.querySelector(".items-ordered-group");
                orderDetails.orderItems.forEach(item => {
                    console.log("Item:", item);
                    const itemRow = document.createElement("div");
                    itemRow.classList.add("item-row");
                    itemRow.innerHTML = `
                        <label><strong>${item.item.name}</strong>: ${item.item.price} € x ${item.quantity}</label>
                        <span>Total: ${item.totalPrice} €</span>
                    `;
                    itemsOrderedContainer.appendChild(itemRow);
                });

                subOrder.appendChild(subOrderItem);
            } catch (orderError) {
                console.error(`Error loading details for sub-order ${orderId}:`, orderError);
                const errorMessage = document.createElement("p");
                errorMessage.textContent = `Failed to load sub-order with ID: ${orderId}`;
                subOrder.appendChild(errorMessage);
            }
        }
    } catch (error) {
        console.error("Error loading group order summary:", error);
        subOrder.innerHTML = "<p>Failed to load data. Please try again later.</p>";
    }
}


async function initConfirmationPage() {
    try {
        const orderDetails = await getOrderById(ORDER_ID);

        if (orderDetails) {

            await populateConfirmationPage(orderDetails);
        } else {
            alert("No order details found.");
        }
    } catch (error) {
        console.error("Error fetching order details:", error);
        alert("Unable to load order details. Please try again later.");
    }
}


initConfirmationPage().then(r => console.log("Confirmation page initialized."));

if (groupId){
    document.getElementById("group-order-summary").style.display = "block";
    document.getElementById("confirm-group-order").style.display = "block";
    loadGroupOrderSummary().then(r => console.log("Group order summary loaded."));
}

const confirmGroup = document.getElementById("confirm-group-order");

confirmGroup.addEventListener("click", async () => {
    const orderId = sessionStorage.getItem("orderId");

    if (orderId) {
        try {
            const groupOrder = await confirmGroupOrder(orderId);
            if (groupOrder) {
                alert("Group order confirmed.");
                confirmGroup.textContent = "Group order confirmed";
                confirmGroup.disabled = true;
                confirmGroup.style.cursor = "not-allowed";

            } else {
                alert("Failed to confirm group order.");
            }
        } catch (error) {
            console.error("Error confirming group order:", error);
            alert("Failed to confirm group order. Please try again later.");
        }
    }
    else {
        alert("No order found.");
    }
});

const back = document.getElementById("back");
back.addEventListener("click", () => {
    sessionStorage.clear()
    window.location.href = "index.html";

});

document.getElementById("go-account").addEventListener("click", () => {
    window.location.href = "account.html";
});