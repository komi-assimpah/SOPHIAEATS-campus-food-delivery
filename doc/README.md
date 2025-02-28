# Rapport de Conception Logicielle

## Équipe H

- **Sagesse ADABADJI**: PO (Product Owner)
- **Sara TAOUFIQ**: SA (Software Architect)
- **Jean Paul ASSIMPAH**: QA (Quality Assurance)
- **Selom Ami ADZAHO**: QA (Quality Assurance)
- **Abenezer YIFRU**: OPS (Operations)

## Sommaire

1. [Périmètre Fonctionnel : Limites, Extensions, Points Forts et Points Faibles](#1.-Périmètre-Fonctionnel-:-Limites,-Extensions,-Points-Forts-et-Points-Faibles)
2.  [Concernant notre architecture](#2.-Concernant-notre-architecture)

## 1. Périmètre Fonctionnel : Limites, Extensions, Points Forts et Points Faibles

### 1.1 Hypothèses de Travail

Le developpement de notre application est basé sur les hypothèses clés suivants:

- Hypothèse 1 : Seuls les restaurants disponibles sont proposés au client. Un temps de préparation moyen de quinze minutes est prévu pour une commande dans un restaurant, ce qui sert pour la gestion de la capacité.

- Hypothèse 2 : L’affichage des heures de livraisons possibles se fait en fonction des horaires du restaurant.

- Hypothèse 3 : Les heures de livraison ont un intervalle de 15 minutes qui correspond au temps de préparation moyen d’une commande.

- Hypothèse 4 : Lorsque l’utilisateur sélectionne une heure de livraison, le temps de préparation total de sa commande doit correspondre à l’heure de livraison choisie.

- Hypothèse 5 : L’utilisateur peut changer l’heure de livraison mais dans la limite de celles disponibles Hypothèse 6 : L’utilisateur choisit un moyen de paiement parmi ceux prédéfinis 

---

### 1.2 Points non implémentés relativement à la spécification et aux extensions requises

Lorsqu’une personne passe une commande en ayant choisi une date de livraison au préalable, les menu Items ont donc pu être disponible en fonction de la capacité du restaurant mais s’il attend l’heure de livraison avant de valider sa commande...   

---

### 1.3 Fonctionnalités : 
![image](https://hackmd.io/_uploads/S1ONOpESyg.png)

#### **Issues Associées**:

---


#### **[US] Implementation of Status Discount Strategy #74**

- **GitHub Issue ID**: [#74](https://github.com/PNS-Conception/STE-24-25--teamh/issues/74)
- **User Story**: As a restaurant manager, I want to apply a status-based discount for specific customer groups (e.g., CAMPUS_STUDENT, LOYAL_CUSTOMER) so that eligible customers can benefit from reduced pricing based on their status.
- **Feature File**: `statusDiscount.feature`

#### **[US] Restaurant Opening Hours Management**

- **GitHub Issue ID**: [#8](https://github.com/PNS-Conception/STE-24-25--teamh/issues/8)
- **User Story**: As a Restaurant Manager, I want to manage the restaurant's opening hours so that customers can view accurate information on when the restaurant is open.
- **Feature File**: `RestaurantScheduleManagement.feature`

#### **[US] Group Order Validation**

- **GitHub Issue ID**: [#77](https://github.com/PNS-Conception/STE-24-25--teamh/issues/77)
- **User Story**: As a Campus User, I want to validate our Group Order so that it moves forward in the delivery process.
- **Feature File**: `groupOrderValidation.feature`
---

## 2. Concernant notre architecture

La séparation du projet a été faite sur la branche [(cliquez ici)](https://github.com/PNS-Conception/STE-24-25--teamh/tree/modules-separation), nous avons cependant manqué de temps pour l'inclure sur la branche main 
    
### Explication du refactoring et diagrammes(classe prio et séquence)

> 
Au début du développement du backend, sur les conseils de nos professeurs, nous avions choisi d’utiliser un **Facade Pattern** pour simplifier les interactions entre les différents composants du système. Cette approche centralisait les appels vers le domaine et proposait une API unifiée pour les clients internes ou externes.

Cependant, avec l'ajout de nouvelles fonctionnalités au fil du développement en BDD, le système est devenu de plus en plus complexe. La **Facade** s’est transformée en une **"God Class"**, regroupant trop de responsabilités. Cette centralisation excessive a entraîné plusieurs problèmes :
- **Complexité** : La Facade était difficile à comprendre et à modifier.
- **Manque de modularité** : Toutes les fonctionnalités étant regroupées dans une seule classe, il était impossible de travailler efficacement sur des fonctionnalités spécifiques, surtout en mode agile.
- **Testabilité réduite** : Tester des fonctionnalités impliquait de gérer de nombreuses dépendances.
- **Difficulté d’évolution** : Chaque modification risquait d’introduire des régressions dans d'autres parties du code.

Ces problèmes limitaient la maintenabilité et la scalabilité du système, ce qui nous a poussé à repenser l’architecture.

### Motivations pour le changement
Après avoir analysé les problèmes et exploré des solutions, nous avons décidé d’adopter la **Clean Architecture**, qui répondait mieux à nos besoins. Voici les raisons principales de ce choix :
1. **Respect des principes SOLID** : Découper les responsabilités (**SRP**) et inverser les dépendances (**DIP**) pour une meilleure structuration
2. **Augmentation de modularité** : Faciliter le développement en séparant les fonctionnalités en services spécifiques, ce qui améliore l'indépendance et la réutilisabilité des composants
3. **Testabilité améliorée** : Isoler les services pour faciliter les tests unitaires et limiter la complexité des mocks
4. **Maintenabilité** : Permettre l’ajout de nouvelles fonctionnalités sans affecter les composants existants.

### Transition vers Clean Architecture
La refactorisation a introduit une **couche application**, nommée `usecase`, composée de plusieurs services dédiés à des cas d’utilisation spécifiques. Chaque service agit comme une **Facade spécialisée**, regroupant uniquement les responsabilités liées à son domaine. Cela nous a permis de :
- Découper les cas d’utilisation (**use cases**) en services légers et indépendants.
- Réduire la complexité en distribuant les responsabilités.
- Clarifier les interfaces et points d’entrée pour les clients.

### Avantages obtenus
La refactorisation a transformé notre codebase avec les bénéfices suivants :
- **Structure claire** : Les responsabilités sont bien séparées entre les services
- **Facilité de maintenance** : Les services peuvent être modifiés ou remplacés facilement grâce à l’utilisation des interfaces
- **Tests simplifiés** : Les tests unitaires et d’intégration peuvent se concentrer sur une fonctionnalité spécifique en mockant le reste
- **Flexibilité** : Ajouter ou modifier des fonctionnalités est plus simple grâce à la modularité

Le passage à la **Clean Architecture** a résolu les limitations de notre ancienne implémentation basée sur un **Facade Pattern** unique. Cette refactorisation a amélioré la qualité, la maintenabilité et la scalabilité de notre application.

![class-diagram (1)](https://hackmd.io/_uploads/HknmcOEHJx.png)


---

## Structure du Code

Notre architecture logicielle est structurée en cinq packages principaux, avec une section supplémentaire dédiée aux tests unitaires et d'intégration :

### `application`
Ce package centralise la logique métier de l'application. Il est subdivisé en plusieurs sous-packages dont les principaux sont `usecase`, qui est lui-même contient `service` et `coordinator`. Les services encapsulent les règles métier spécifiques tandis que les coordinateurs orchestrent les interactions entre différents services. Ce package contient également les Data Transfer Objects (DTO) qui facilitent le transfert de données à travers les différentes couches de l'application.

### `domain`
Regroupe les entités et les modèles de données essentiels tels que les commandes, les restaurants, ou encore les groupes de commandes. Ce package définit les objets fondamentaux manipulés par l'application et est crucial pour comprendre les interactions et les règles métier de base.

### `frontend`
Pour les développeurs qui travaillent sur les aspects visuels et interactifs de l'application, ce package est crucial. Il contient tous les éléments relatifs à la gestion de l'interface utilisateur, y compris le CSS, HTML et JavaScript. De plus, il inclut des utilitaires spécifiques pour la gestion des restaurants et des interactions avec l'API.


### `infrastructure`
Ce package intègre les différentes configurations de persistence des données. Il comprend des `repository` utilisant la méthode `inMemory` pour un stockage des données local et temporaire, idéal pour les tests et le développement. Il inclut également une intégration avec Firebase pour une gestion des données à distance via une base de données en temps réel.

### `server`
Contient la configuration et la gestion des serveurs, incluant les gestionnaires de requêtes et notre `ApiGateway`. Ce package est essentiel pour la mise en place de l'infrastructure serveur et la gestion des interactions réseau, assurant la réception et le traitement efficace des requêtes.

La structure de notre projet est pensée pour faciliter la maintenance et l'évolution de l'application, tout en offrant une clarté maximale aux développeurs qui interviendront sur le projet.

<p align="center">
  <img src="https://hackmd.io/_uploads/rJl_uF4ryl.png" alt="Structure des dossiers"/>
</p>


---

### Décomposition en Services

Nous avons structuré notre système en plusieurs services distincts, chacun conçu avec une faible dépendance pour répondre à des besoins spécifiques et permettre des extensions futures, telles que l'authentification, la gestion des restaurants, ou la livraison. Voici un aperçu des services principaux :

- **Order Service** : Gère les commandes individuelles.
- **GroupOrder Service** : Permet la gestion des commandes de groupe.
- **Location Service** : Offre des fonctionnalités de localisation.
- **UserService** : Gère les informations des utilisateurs.
- **RestaurantService** : Dédié à la gestion des restaurants.

Chaque service est équipé de sa propre API, qui inclut un serveur, un gestionnaire, et des endpoints spécifiques. Cette structure modulaire permet à chaque service d'évoluer indépendamment en fonction des besoins changeants.

Nous avons également mis en place un **Coordinateur**, un composant crucial qui facilite les interactions complexes entre plusieurs services. Ce coordinateur travaille directement avec les différents services pour orchestrer des réponses impliquant diverses fonctionnalités. (Je vais peut être enlever le mot otchestrer)

---

### Entités et Objets de Communication

Dans notre architecture, les entités et les Data Transfer Objects (DTOs) jouent un rôle central dans la gestion et la communication des données. Voici leur description détaillée :

#### **Entités**

Les entités représentent les objets persistants de l'application. Elles modélisent les concepts clés du domaine et sont utilisées pour les opérations métier principales. Voici les entités présentes dans le système :

- **DeliveryLocation** : Gère les informations relatives aux lieux de livraison.
- **GroupOrder** : Représente un ensemble de commandes regroupées.
- **Order** : Représente une commande individuelle.
- **OrderItem** : Définit un élément spécifique au sein d'une commande.
- **PaymentDetails** : Stocke les informations liées au paiement.
- **MenuItem** : Modélise un élément du menu proposé par un restaurant.
- **Restaurant** : Contient les informations détaillées sur les restaurants partenaires.
- **Schedule** : Définit les horaires et disponibilités des restaurants.
- **User** : Représente les utilisateurs de l'application, qu'ils soient clients ou administrateurs.
- **Address** : Modélise les adresses utilisées pour la livraison ou les informations utilisateur.

#### **DTOs (Data Transfer Objects)**

- **AddressDTO** : Transporte les informations sur les adresses.
- **DeliveryLocationDTO** : Contient les données sur les lieux de livraison.
- **GroupOrderDTO** : Sert à transmettre les informations sur les commandes de groupe.
- **OrderDTO** : Transporte les détails d'une commande.
- **RestaurantDTO** : Gère les données des restaurants lors des interactions entre services.
- **UserDTO** : Transfère les informations des utilisateurs, comme le profil (nom , prénom , adresse mail , balance ... ) .

Chaque entité et DTO est conçu pour répondre à des besoins spécifiques tout en assurant une gestion et une communication efficaces des données dans l'ensemble du système.

---

### Décomposition en 2 projets


#### Communication entre projets :
Le gateway présent dans le module OrderService orchestre toutes les requêtes de l'application et envoie des requêtes spécifiques au module GroupOrderService. Les réponses récupérées sont ensuite utilisées dans les séquences de requêtes réalisées par l'orchestrateur.
#### Données échangées :
Pour éviter la duplication de classes dans les différents modules, les objets du module OrderService sont représentés par leurs identifiants. Les données échangées sont donc des types simples que sont les Strings et les dateTimes. 
#### Dépendances :
Pour certains tests de la classe façade du backend qui utilisait des types complexes du module GroupOrderService, ce module a dû être importé le temps de représenter également ses objets complexes par des types simples dans le module OrderService.

---

### APIs Utilisées

#### API Gateway
Pour fournir une porte d'entrée unique à notre backend pour toutes les requêtes client, nous avons mise en place un API Gateway (`http://localhost:8080/api`). Ce dernier  orchestre les appels aux différents services sous-jacents, notamment les services de gestion des restaurants, des horaires et des commandes groupées.


![image](https://hackmd.io/_uploads/rJKNw07SJx.png)
![image](https://hackmd.io/_uploads/BJnA3RQr1g.png)

[Cliquez ici pour consulter la documentation complete openAPI](https://github.com/PNS-Conception/STE-24-25--teamh/blob/server/openapi.yaml)


#### Services internes
- **Service Restaurant**
    Responsable de la gestion des restaurants et des horaires associés.
    - Créer, mettre à jour, et récupérer les informations des restaurants.
    - Gérer les horaires d'ouverture et de disponibilité des restaurants.

- **Service Location**
    Responsable de la gestion des emplacements de livraison.
    - Ajouter de nouveaux emplacements de livraison.
    - Lister et rechercher les emplacements.
    - Récupérer les détails d'un emplacement spécifique.


- **Service Order**
    Responsable de la gestion des commandes individuelles.
    - Créer, mettre à jour, et suivre les commandes.
    - Gérer les états des commandes, tels que "en attente", "en préparation", et "livrée".

- **Service GroupOrder**
    Responsable de la gestion des commandes en groupe
    - Créer des groupes pour regrouper plusieurs commandes individuelles.
    - Ajouter ou retirer des commandes à un groupe existant.
    - Valider, confirmer, et finaliser les commandes groupées.

- **Service User**
    Responsable de la gestion des utilisateurs et des profils.
    - Enregistrer de nouveaux utilisateurs et gérer leurs informations.dif
    - Fournir des fonctionnalités d'authentification et d'autorisation pour sécuriser l'utilisation de l'application.


---

### Interfaces Utilisateurs

### 1. Structure Générale
Le frontend de ce projet se compose de plusieurs pages HTML interconnectées, axées sur la gestion de commandes, notamment dans un contexte de groupe. Les principales caractéristiques et fonctionnalités de l'interface utilisateur incluent :

- Navigation intuitive entre les différentes pages.
- Possibilité de créer ou rejoindre des commandes groupées.
- Gestion des commandes simples ou en groupe avec des interactions dynamiques (popups, filtres, etc.).


### 2. Pages Principales

#### **Page d'accueil (`index.html`)**
#### **Objectif** : 
Fournir un aperçu des restaurants disponibles et permettre les actions suivantes :
- Navigation vers une liste plus détaillée des restaurants.
- Création d’une commande groupée.
- Participation à une commande de groupe existante.
- Placement d'une commande simple en cliquant directement sur un restaurant

![image](https://hackmd.io/_uploads/HkjSvjQBkx.png)




#### **Page de liste des restaurants (`restaurantList.html`)**
#### **Objectif** :
Afficher une liste complète et détaillée des restaurants.

#### **Éléments principaux** :
- **Liste de cartes dynamiques** :
  - Seuls les restaurants disponibles à l'heure 'actuelle' sont affichés
  - Les utilisateurs peuvent :
    - Passer une commande simple directement depuis cette page.
    - Être redirigés vers cette page après avoir créé ou rejoint une commande groupée.

![image](https://hackmd.io/_uploads/rk8yWGNHkl.png)



#### **Page des menus (`orderPage.html`)**
#### **Objectif** :
Afficher les menus disponibles pour un restaurant, en fonction de l’heure de livraison choisie.

#### **Éléments principaux** :
- **Menu interactif** : Les plats disponibles sont filtrés en fonction de l’heure sélectionnée.
- **Sélection des plats** : L’utilisateur peut ajouter des items à son panier pour passer une commande.

![image](https://hackmd.io/_uploads/BkHXWM4BJl.png)



#### **Page de résumé et de paiement (`checkoutSummary.html`)**
#### **Objectif** :
Permettre à l'utilisateur de finaliser sa commande et de procéder au paiement.

#### **Éléments principaux** :
- **Résumé de la commande** : Affichage des items sélectionnés, des quantités et du prix total.
- **Paiement** : Bouton permettant de payer la commande.
- Possibilité de fermer une commande en groupe

![image](https://hackmd.io/_uploads/BJYe7G4SJx.png)



#### **Page de confirmation de paiement (`paiementConfirmation.html`)**
#### **Objectif** :
Afficher une confirmation après le paiement, avec un résumé des commandes dans le cas d'une commande en groupe.

#### **Éléments principaux** : 
- Prix total de la commande
- Le solde de l'utilisateur qui est mis à jour en cas de réduction
- Notification de réduction
- Résumé des commandes dans une commande en groupe

![image](https://hackmd.io/_uploads/ryK7LfNS1e.png)


#### **Page Compte Utilisateur (`account.html`)**

#### **Objectif**
Offrir à l'utilisateur une vue d'ensemble sur les informations liées à son compte et ses interactions avec le service.


#### **Éléments principaux**

- Informations personnelles
- Solde actuel
- Option pour se déconnecter.

![image](https://hackmd.io/_uploads/HkGZjMNByx.png)

Ce frontend est conçu pour offrir une expérience utilisateur fluide, intégrant des interactions dynamiques pour gérer efficacement les commandes individuelles ou de groupe. Les popups et les filtres ajoutent une couche supplémentaire d’interactivité, tandis que l’intégration avec une API permet d’afficher des informations précises et en temps réel. 

---

### Cheminement de Requêtes

#### **Récupération des Restaurants** :

La récupération des restaurants est une fonctionnalité essentielle permettant aux utilisateurs de consulter les options disponibles. Cette requête suit un chemin structuré, partant de l'interface web jusqu'à la couche de domaine, en passant par les différentes couches de notre architecture Clean Architecture.
La requête suit les étapes suivantes:

1. **Interface utilisateur (Frontend)** : 
    - L'utilisateur initie une requête via une interface web (ou mobile), en allant sur la page d’accueil (qui doit normalement afficher la liste des restaurants). 
    - Une requête HTTP est envoyée à l'API Gateway avec les éventuels paramètres (par exemple, le nom du restaurant en cas de recherche). Dans le cas décrit ci-dessous, on récupère tous les restaurants (ouverts et fermés)

2. **API Gateway** : 
    - L'API Gateway reçoit la requête et la transmet au micro-service (**contrôleur**) approprié. 

3. **Contrôleur (HttpHandler)** : 
    - Le contrôleur analyse la requête et appelle le **cas d’utilisation (use case)** correspondant via un service dédié (`RestaurantService`). 

4. **Service de cas d’utilisation (Use Case)** : 
    - Le service orchestre la logique métier en déléguant la récupération des données à un **repository** via une interface abstraite (`IRestaurantRepository`). 
    - Cette couche applique également toute règle métier spécifique (par exemple, filtrage selon la disponibilité des restaurants, selon le nom…etc).

5. **Repository (Infrastructure)** : 
    - Le repository interagit avec la source de données (par exemple, un stockage en mémoire  ou une base de données firebase). 
    - Il exécute les requêtes nécessaires pour récupérer les informations sur les restaurants.

6. **Retour des données** : 
    - Les données récupérées (liste de modèle **Restaurant**) sont renvoyées 
    - Les couches supérieures transmettent ces données jusqu'à l'interface utilisateur, où elles sont éventuellement convertit en format Json puis afficher par le frontend

![recuperaction-des-restaurants](https://hackmd.io/_uploads/HyEP5LmHJl.png)


#### **Prise de Commandes (Ajout des menus Items)** : 

1. **Interface utilisateur (Frontend)** 
   - L'utilisateur sélectionne un restaurant, fournit les informations de livraison (adresse et heure de livraison), et accède à la page des menu items. 
   - Sur cette page, les menu items disponibles sont filtrés en fonction de l'heure de livraison choisie. 
   - L'utilisateur ajoute les items souhaités au panier. 
   - À chaque ajout d’un menu item, une requête HTTP est envoyée au API Gateway pour mettre à jour la commande en cours.

2. **API Gateway** 
   - L’API Gateway reçoit la requête et la transfère au contrôleur adapté.

3. **Contrôleur (OrderHttpHandler)** 
   - Le contrôleur appelle le `OrderCoordinator` pour gérer la mise à jour de la commande en ajoutant les menu items sélectionnés.

4. **Service de cas d’utilisation (Use Case)** 
   - Le `OrderCoordinator` vérifie si la commande existe et appelle le `OrderService` pour appliquer les modifications. 
   - Le `OrderService` utilise le repository pour enregistrer les nouvelles données de la commande, incluant les menu items ajoutés.

5. **Repository (Infrastructure)** 
   - Le repository interagit avec la base de données pour enregistrer les modifications apportées à la commande, incluant l’ajout des menu items.

6. **Retour des données** 
   - Les données mises à jour (commande avec les menu items ajoutés) sont renvoyées à l’interface utilisateur pour que le panier ou la commande soit mis à jour.
  
![image](https://hackmd.io/_uploads/ryyYmdNrJg.png)

___

### Design Pattern 


### **Notre projet actuel comporte:**

**Un startegy pattern :**
![strategy pattern](https://hackmd.io/_uploads/rkhwf5VBkg.png)



**Un builder design :** 

Après avoir hésité entre le Builder, le Composite et le Factory, nous avons choisi le Builder, qui s’adaptait mieux à notre refactoring et permettait une construction progressive des commandes. 

![builder pattern](https://hackmd.io/_uploads/B1aNVqVSkg.png)



**Un facade pattern :** 

On savait qu’il nous fallait une façade dans notre projet. Elle est passée par plusieurs évolutions. Au départ, elle contenait des managers comme attributs, puis nous sommes passés aux entités elles-mêmes, pour finalement opter pour le passage des entités en paramètres. 

![facade pattern](https://hackmd.io/_uploads/BkVrBcNS1e.png)

---

### Base de données
Concernant la gestion des bases de données dans notre projet, deux approches ont été adoptées après le refactoring qui a pris en compte le besoin d'une base de données :

**Gestion de données locales :**
Dans le dossier infrastructure, vous trouverez le sous-dossier inmemory, qui correspond au repository local. Les données y sont stockées directement sur la machine locale, ce qui empêche leur partage avec d'autres systèmes.

**Gestion de données partagées:**
L'ajout de Firebase s'est déroulé de manière relativement fluide.  Une difficulté notable concernait la compatibilité entre Firebase et le type LocalTime de Java depuis la nouvelle version. Cela a entraîné des ajustements pour adapter le format des données et garantir leurs bon fonctionnement.


---
---

## 3. Qualité des Codes et Gestion de Projets

### Types de Tests
Pour nous assurer de la fiabilité et de la robustesse de notre application, nous avons mis en place différentes sortes de tests tels que : 
- **Des Tests unitaires Junit**: Chaque classe a été testé de manière isolée pour s'assurer que les méthodes respectent les comportements attendus. Ces tests, réadaptés suite aux modifications de l'architecture, couvrent les composants essentiels tels que les services internes et les handlers.
- **Des tests Fonctionnels avec Cucumber** : À travers des scénarios écrits en Gherkin, nous avons testé des cas d'utilisation précis, pour nous assurer du comportement attendu de bout en bout lors de l'utilisation de l'application par les acteurs. Ces tests facilitent la collaboration entre développeurs et non-développeurs grâce à leur lisibilité.
- **Des tests d'Intégration** : Nous avons intégré des tests pour valider la communication entre nos services et l'API Gateway, simulant des scénarios réels d'échange de données avec Mockito. Cela garantit une fluidité dans les interactions au sein de notre système.
- **Tests de Gestion des Erreurs** : Nous avons veillé à inclure des tests exhaustifs des mécanismes de gestion d'erreurs où nous simulons différents cas d'erreur (requêtes malformées, ressources inaccessibles) pour vérifier que chaque service réagit de manière appropriée. Ces tests assurent que les exceptions sont correctement capturées, les erreurs clairement identifiées, et que les codes de statut HTTP retournés correspondent aux standards (400 pour les requêtes incorrectes, 404 pour les ressources non trouvées, 500 pour les erreurs serveur).

---

### Qualité des Codes

Pour écrire du code de qualité, nous avons adopté une démarche articulée autour des principes suivants : 
- **Lisibilité et Simplicité** : Nos méthodes sont courtes et centrées sur une seule responsabilité, facilitant leur compréhension et leur maintenance.
- **Réutilisabilité** : Les constantes et les fonctions partagées ont été centralisées dans des classes utilitaires, réduisant la duplication de code et facilitant la maintenance.
- **Modularité** : Nous utilisons des interfaces pour définir les interactions entre les services ce qui rend notre architecture flexible et facilement extensible.
- **Robustesse** : Une gestion rigoureuse des exceptions, couplée à des contrôles systématiques, garantit la stabilité de l'application face aux situations exceptionnelles.
- **Traçabilité** : Les loggers suivent chaque requête, chaque réponse, et chaque erreur, offrant une visibilité complète et facilitant le débogage.
- **Collaboration** : La documentation OpenAPI renforcent la collaboration entre les membres de l’équipe et facilitent la prise en main par de nouveaux développeurs.






---
### Gestion du Projet

Dès le début, pour la gestion des branches, nous avons adopté une stratégie Git basée sur GitFlow. La branche main est dédiée aux versions stables et prêtes pour la production. Aucune modification ne peut y être directement intégrée sans validation préalable. La branche develop est utilisée pour intégrer les nouvelles fonctionnalités. Elle représente un environnement de travail stable mais non destiné directement à la production.

Les nouvelles fonctionnalités sont développées dans des feature branches spécifiques tirés à partir de develop, chacune correspondant à une tâche ou une user story. Ce modèle garantit que plusieurs développeurs peuvent travailler simultanément sur des fonctionnalités différentes sans interférer les uns avec les autres. 

Chaque fonctionnalité est développée dans une branche dédiée, à partir de la branche develop. Une fois la fonctionnalité terminée, une pull request est soumise pour fusionner la feature branch dans develop. À ce stade, des revues de code et des tests automatisés sont exécutés avant d’autoriser la fusion. Une fois validée, la branche feature est fusionnée dans develop, garantissant que seul du code testé et revu est intégré.

Au début du projet, nous avons constaté que certaines issues tentaient de résoudre trop de fonctionnalités d’un coup, rendant difficile la gestion et le suivi de leur avancement. Cela engendrait également des problèmes lors des revues de code, où de nombreux changements étaient mélangés, compliquant la validation de chaque partie.

Pour améliorer cette situation, nous avons introduit une approche de découpage vertical des fonctionnalités. L'idée était de diviser les grandes tâches en plus petits morceaux, chacun correspondant à une fonctionnalité distincte et directement testable. Par exemple, au lieu d'une seule issue pour " Gestion de l'information sur les restaurants (heures d'ouverture et menu)", nous avons créé deux issues séparées : l'une pour la gestion des horaires et l'autre pour la gestion du menu.

Chaque issue devient alors plus facile à gérer, à tester, et à valider individuellement. Cela a permis de mieux suivre l’avancement des fonctionnalités tout en facilitant les revues de code, car chaque pull request était beaucoup plus concise et précise. Ce découpage vertical nous a également aidés à mieux prioriser les fonctionnalités critiques pour le projet et à livrer plus rapidement des versions fonctionnelles aux utilisateurs.

---
---

## 4. Rétrospective et Auto-évaluation

### Rétrospective

#### Sagesse ADABADJI - PO (Product Owner)
- **Rôle et Implication**: 
Ma participation en tant que Product Owner a été de veiller à ce que le produit réponde aux exigences fonctionnelles définies et de prioriser les besoins du projet en fonction des attentes définies en matière de technologie et celles de chaque groupe d'utilisateurs.

- **Réalisations**:
-Gestion du backlog : Création, mise à jour et priorisation du backlog produit afin de garantir que les fonctionnalités les plus critiques soient développées en premier.
-Vérification et clarification des exigences des utilisateurs finaux, traduisant ces attentes en user stories claires et exploitables.
-Planification des sprints pour que les objectifs restent alignés avec l'état du produit et les capacités de l’équipe.
-Validation des fonctionnalités : Vérification de tests d'acceptation pour confirmer que les fonctionnalités livrées répondaient aux attentes initiales.

- **Leçons Apprises**:
-Etude des exigences : Prendre le temps d'expliquer et de s'entendre sur les exigences dès le début permet de réduire les risques de malentendus et les efforts inutiles.
-Adaptabilité : Être prêt à ajuster les priorités en fonction des imprévus et des retours des utilisateurs est crucial pour garantir la valeur du produit final.
-Collaboration : Garder une communication transparente et régulière entre tous les membres de l’équipe améliore la dynamique de l'équipe et permet de progresser sur le projet avec moins de difficultés.

- **Erreurs**:  
-Planifier est important mais il n'est pas possible de tout définir dès le départ. Tout définir plus ou moins clairement avant de commencer peut faire perdre du temps.
-Compte tenu du manque de temps vers la fin, le développement de certaines fonctionnalités en cas d'imprévus comme des bugs a pris plus de temps que prévu. Des délais supplémentaires d'assurance dès le départ auraient pu éviter le remue-méninges des derniers instants 

#### Sara TAOUFIQ - SA (Software Architect)
- **Rôle et Implication**:

En tant que Software Architect, j’ai assuré la cohérence et la solidité de l’architecture du projet. J’ai supervisé la conception et l’évolution des diagrammes de classe et de séquence pour qu’ils reflètent les besoins fonctionnels.

J’ai guidé l’intégration des design patterns, notamment en résolvant les défis liés au pattern “Facade”. J’ai également dirigé le passage à une clean architecture en définissant les étapes du refactoring et en intégrant des repositories et interfaces.

Enfin, j’ai orchestré la modularisation en services, validé l’intégration des APIs, et assuré une gestion fluide des données locales et distantes.



- **Réalisations**:

***Diagrammes de classe et de séquence***

***Design patterns***

***Clean architecture***

***Modularisation en services***

***Gestion des données locales et distantes***

***Intégration des APIs***


- **Leçons Apprises**:
1. Prendre **vraiment** le temps de bien structurer et concevoir le projet avant de se lancer dans le code. 
2. Il n'y a pas qu'une seule manière de faire et les demandes du clients peuvent rapidement changées , il faut donc être pret à être flexible à tous moments. 




- **Erreurs**: 
     1. En vue de mon manque de connaissances initiales sur l’implémentation d’une nouvelle architecture. Cela nous a conduit à démarrer le travail dans une mauvaise direction, et nous avons dû, par la suite, effectuer un refactoring important, ce qui nous a coûté beaucoup de temps.
    2. Concevoir une bonne architecture est important, mais répartir efficacement les tâches l'est encore plus. À un certain stade, nous nous sommes retrouvés à travailler chacun sur une seule et unique partie du projet : un membre A travaillait exclusivement sur le frontend, tandis qu’un membre B  se consacrait uniquement à la gestion des données partagées. Sur le moment c'était la seule manière pour nous d'avancer cependant cela a conduit à des pull requests totalement différentes, rendant leur fusion laborieuse.
 
 
 
 
 


#### Jean Paul ASSIMPAH - QA (Quality Assurance)

**Rôle et Implication**: 
En tant que QA, mon rôle était de garantir la qualité globale du projet à travers l'écriture de tests robustes, la mise en place et le suivi des bonnes pratiques de codage. J'ai également veillé à ce que l'application soit conçue pour résister à des cas d'erreur, tout en étant maintenable et évolutive.
  
**Réalisations**:
- Mise en place de tests Cucumber : L’utilisation de tests Cucumber a permis de couvrir les scénarios fonctionnels critiques en adoptant un langage compréhensibleau grand public. Ces tests ont facilité une validation collaborative des fonctionnalités principales et permis une détection précoce des anomalies. 
- Mise en place des tests JUnit : L'écriture de ces tests nous permis de valider avec rigueur le comportenant de nos différentes classes
- Mise en place des tests d'intégration pour valider la communication entre nos services et l'API Gateway, garantissant une expérience fluide pour les utilisateurs.
- Validation des conventions de codage et centralisation des fonctions partagées dans des classes utilitaires pour renforcer la maintenabilité et la lisibilité.
- Fourniture d’une documentation OpenAPI claire et complète pour simplifier les tests et l’accès aux fonctionnalités exposées par l'API Gateway.


- **Leçons Apprises**: 
    - L'importance de la traçabilité avec les logs pour déboguer efficacement des systèmes complexes.
    - La mise en place des tests dès le début d'un projet pour éviter de long moment de débogage et l'amélioration de code

- **Erreurs**:
- Nous avons configuré et installé SonarQube uniquement sur la branche principale (main), ce qui n’est pas une approche optimale. Cette décision a conduit à une accumulation de dette technique, identifiée tardivement, nécessitant un rattrapage intensif sur une période de 6 jours en fin de projet.
- Après la première phase du projet, un relâchement dans l’écriture rigoureuse des tests a été observé. Cela a entraîné une couverture moins homogène des nouvelles fonctionnalités développées durant cette période, ce qui a nécessité un effort supplémentaire de rattrapage lors de la phase finale.

#### Selom Ami ADZAHO - QA (Quality Assurance)
- **Rôle et Implication**: 
En tant que QA, mon rôle était d’assurer la qualité et la fiabilité du projet. Les principales responsabilités incluaient :
    - **Analyse et validation** des fonctionnalités pour m’assurer qu’elles étaient claires et testables. 
    - **Rédaction de scénarios de test** pour couvrir les cas principaux et limites.
    - **Détection et suivi des anomalies** pour faciliter leur correction rapide.
    - **Amélioration continue** de la robustesse et de la maintenabilité de l’application. 
    
- **Réalisations**: 
    - **Mise en place de tests ciblés (Cucumber et JUnit)** :
  Développement de tests couvrant des fonctionnalités clés, notamment la gestion des commandes simples, pour détecter rapidement les anomalies et renforcer la fiabilité du système.
    - **Utilisation stratégique de Mockito pour les tests d'intégration des serveurs** :
  Mise en œuvre de tests simulant des comportements précis afin de minimiser les dépendances avec d’autres composants du système, assurant ainsi des validations rapides et isolées.
    - **Suivi et validation rigoureuse des corrections** :
  Validation systématique des correctifs pour prévenir les régressions, garantir la stabilité du produit et maintenir une qualité constante.
    - **Amélioration de l’expérience utilisateur** :
  Tests ciblés sur les interfaces utilisateur pour identifier et corriger les incohérences, offrant une expérience fluide et intuitive adaptée aux attentes des utilisateurs finaux.

- **Leçons Apprises**: 
    - **Importance des tests en amont** : L’intégration continue des tests dans le cycle de développement permet d’identifier les problèmes rapidement, réduisant ainsi les coûts de correction.
    - **Automatisation stratégique** : Automatiser les tests pour les fonctionnalités critiques ou à forte récurrence est un investissement rentable à long terme.
    - **Importance générale des tests** : Les tests jouent un rôle crucial dans l'assurance qualité du code et renforcent la fiabilité du produit.

- **Erreurs**: 
    - **Rythme irrégulier** : Après un bon démarrage, la cadence des tests a ralenti dans la seconde moitié du projet, entraînant un manque de constance dans la couverture.
    - **Utilisation partielle de tests Mock** : Bien que certains tests aient utilisé Mockito, leur adoption n'a pas été systématique. Une utilisation plus généralisée aurait simplifié l'adaptation des tests lors des modifications des repositories, réduisant ainsi les réajustements nécessaires

#### Abenezer YIFRU - OPS (Operations)

- **Rôle et Implication**:
En tant qu'Ops, j'ai pris en charge l'aspect qualité et gestion des processus automatisés liés aux tests, et à la gestion des branches.

- **Réalisations**: 
La qualité des tests a été surveillée grâce à des outils tels que JaCoCo pour la couverture et une instance de SonarQube déployé sur le cloud pour l'analyse statique du code. Ces outils ont permis de détecter et corriger des failles potentielles et des duplications de code.
J'ai configuré un pipeline d'intégration continue (CI) à l'aide de GitHub Actions. Ce pipeline s'exécute automatiquement à chaque pull request ou validation de code, incluant les étapes suivantes : build du projet, exécution des tests unitaires et tests d'intégration, et analyse statique avec SonarQube pour garantir la qualité du code. Cette automatisation a réduit le temps nécessaire à la validation et intégration des nouvelles fonctionnalités.

- **Leçons Apprises**: 
Mon rôle était important pour la gestion technique du projet mais pour une bonne collaboration, j'ai appris que la communication était cruciale. Le déploiement de SonarQube sur Amazon ec2 a été un plaisir et m'a permit d'apprendre d'avantage l'utilité de Github Actions. Cependant, ceci restait toujours un outil pour améliorer l'automatisation et non la communication transverse.
Concernant la gestion du projet, même si on avait bien organisé les sprints, il a été dès fois difficile de coordonner les merges des branches quand plusieurs développeurs travaillaient sur des fonctionnalités interconnectées.

- **Erreurs**:
Vu le manque de temps, on a souvent intégré du code dans la branche stable sans se poser la question de refactor le code ou sans se demander sur l'extensibilité. Le principe SOLID aurait pu nous aidé refactoré notre code avant l'intégration pour réduire la dette technique. Mais il a fallu balancé de façon pragmatique avec le temps qui nous restait.


---

### Bilan général

- **Fonctionnement de l'Équipe**: [L'équipe fait le bilan de son fonctionnement.]
    - **Milestones** : Chaque fonctionnalité était divisée en étapes bien définies, ce qui nous a permis de suivre nos progrès facilement.
    - **Réunions courtes et fréquentes** : Pendant nos rencontres on faisait des petites réunions (souvent 15 minutes) pour faire le point sur nos avancements et discuter des problèmes rencontrés.
    - **Suivi et mise à jour continue** : Chaque membre mettait à jour le serveur de discussion pour informer sur l'état d’avancement de ses tâches. Cela a aidé à mieux coordonner le travail et garder une bonne visibilité sur les progrès de l’équipe.

- **Améliorations et Réflexions**: [Qu’auriez-vous fait autrement? Qu’aimeriez-vous améliorer?]

    -  [Sara] J'aurais préféré que l'intégration du code dans les branches stables soit plus régulière, plutôt que de faire de grosses pull requests qui obligent les autres à s'adapter difficilement.
    
    -  [Abe] J'aurais aimé passé un peu plus de temps sur la conception du Backend. Vu notre approche BDD, ça aurait été plus intéressant de peaufiner nos tests Cucumber pour que ça soit non seulement lisible pour un non-programmeur mais qui facilement réalisable par un programmeur.
    
    -  [Selom] J’aurais aimé des réunions mieux organisées pour éviter les divergences d’opinions et gagner du temps. Une planification claire et un alignement des objectifs dès le départ auraient aidé à être plus efficaces. Il aurait été utile d’avoir des discussions plus ciblées pour éviter les échanges inutiles qui ralentissent le travail. Nous aurions dû mieux estimer nos capacités et être plus consistants dans nos pushs et pull requests, ce qui aurait permis d'être plus organisé et d'éviter les retards de délais.

    -  [Sagesse] J'aurais aimé que les décisions nécessaires fussent prises dès le départ pour éviter des changements coûteux en temps et des difficultés d'intégration et de mise en commun de toutes les fonctionnalités, notamment les plus importantes
  
    -   [JP] Nous avons configuré et installé SonarQube uniquement sur la branche principale (main), ce qui n’est pas une approche optimale. Cette décision a conduit à une accumulation de dette technique, identifiée tardivement, nécessitant un rattrapage intensif sur une période de 6 jours en fin de projet.



---

### Auto-évaluation

| Sagesse ADABADJI | Sara TAOUFIQ | Jean Paul ASSIMPAH | Selom ADZAHO | Abenezer YIFRU |
|------------------|--------------|--------------------|--------------|----------------|
| 100              |100           | 100                | 100          | 100            |
