import { getUser } from "./utils/apiService.js";

async function fetchUserDetails() {
    try {
        //const userId = sessionStorage.getItem("userId");
        const userId = "2";

        if (!userId) {
            alert("Utilisateur non connecté. Redirection vers la page de connexion...");
            window.location.href = "login.html";
            return;
        }

        const user = await getUser(userId);

        document.getElementById("user-name").textContent = user.name;
        document.getElementById("user-email").textContent = user.email;
        document.getElementById("user-balance").textContent = `${user.balance.toFixed(2)} €`;
        document.getElementById("user-type").textContent = user.type.replace("_", " ");
    } catch (error) {
        console.error("Erreur lors de la récupération des informations utilisateur :", error);
        alert("Impossible de charger les informations utilisateur. Veuillez réessayer.");
    }
}

function setupLogout() {
    const logoutBtn = document.getElementById("logout-btn");
    logoutBtn.addEventListener("click", () => {
        sessionStorage.removeItem("userId");
        alert("Déconnecté avec succès. Redirection vers la page de connexion...");
        window.location.href = "login.html";
    });
}

document.addEventListener("DOMContentLoaded", () => {
    fetchUserDetails().then(r => console.log("User details fetched"));
    setupLogout();
});
