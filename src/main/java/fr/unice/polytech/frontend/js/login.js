import {loginAccount} from "./utils/apiService.js";

async function loginAccountHtml(event) {
    event.preventDefault();

    const email = document.getElementById("email");
    const password = document.getElementById("password");

    if (!email.value || !password.value) {
        alert("Veuillez remplir tous les champs.");
        return;
    }

    const account = {
        email: email.value,
        password: password.value,
    };

    try {
        const user = await loginAccount(JSON.stringify(account));

        sessionStorage.setItem("userId", user.id);
        sessionStorage.setItem('balance', user.balance);
        alert("Connexion réussie !");
        window.location.href = "index.html";
    } catch (error) {
        console.error("Erreur lors de la connexion :", error);
        alert("Échec de la connexion. Veuillez vérifier vos informations.");
    }
}

const loginForm = document.getElementById('login-form');
loginForm.addEventListener('submit', loginAccountHtml);