import { createAccount } from "./utils/apiService.js";

async function createAccountHtml(event) {
    event.preventDefault();

    const name = document.getElementById('name');
    const email = document.getElementById('email');
    const password = document.getElementById('password');

    if (!name.value || !email.value || !password.value) {
        alert('Veuillez remplir tous les champs.');
        return;
    }

    const accountData = {
        name: name.value.trim(),
        email: email.value.trim(),
        password: password.value.trim(),
    };

    try {
        const user = await createAccount(accountData);

        sessionStorage.setItem('userId', user.id);
        sessionStorage.setItem('balance', user.balance);
        alert('Compte créé avec succès ! Redirection vers la page d\'accueil...');
        window.location.href = 'index.html';
    } catch (error) {
        console.error('Error creating account:', error);
        alert('Échec de la création du compte. Veuillez réessayer.');
    }
}

const createForm = document.getElementById('create-form');
createForm.addEventListener('submit', createAccountHtml);