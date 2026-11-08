package com.example.util

import com.example.model.Language

object Strings {
    // App Branding
    const val APP_TITLE = "MOVIE SW"
    const val APP_TAGLINE_SW = "Tazama Filamu, Drama na Mfululizo Mtandaoni"
    const val APP_TAGLINE_FR = "Regardez Films, Drames et Séries en Streaming"

    // Navigation Tabs
    fun navHome(lang: Language) = if (lang == Language.SW) "Nyumbani" else "Accueil"
    fun navExplore(lang: Language) = if (lang == Language.SW) "Chunguza" else "Explorer"
    fun navAlbum(lang: Language) = if (lang == Language.SW) "Albamu" else "Bibliothèque"
    fun navAccount(lang: Language) = if (lang == Language.SW) "Akaunti" else "Compte"
    fun navAdmin(lang: Language) = if (lang == Language.SW) "Admin" else "Admin"

    // Onboarding
    fun onb1Title(lang: Language) = if (lang == Language.SW) "Drama, Action & Love Story" else "Drame, Action & Romance"
    fun onb1Desc(lang: Language) = if (lang == Language.SW)
        "Tazama filamu motomoto za kusisimua, mapigano makali na hadithi za mapenzi zilizobamba zaidi."
    else
        "Découvrez les films les plus captivants, des scènes d'action explosives et de magnifiques histoires d'amour."

    fun onb2Title(lang: Language) = if (lang == Language.SW) "Drama za China na Korea" else "Drames de Chine et de Corée"
    fun onb2Desc(lang: Language) = if (lang == Language.SW)
        "Mfululizo wa kihistoria (Wuxia, K-Drama) na miji ya kisasa kama Seoul na Beijing kwa ubora wa juu."
    else
        "Immersion dans les sagas historiques de Chine, les arts martiaux et les meilleures séries modernes de Séoul."

    fun onb3Title(lang: Language) = if (lang == Language.SW) "Watafsiri Ma-DJ wa Kiswahili" else "Doublage par les Meilleurs DJs"
    fun onb3Desc(lang: Language) = if (lang == Language.SW)
        "Sikiliza sauti za ma-DJ maarufu (DJ Afro, DJ Murphy, DJ Black, DJ Vasco) wakitafsiri kwa Kiswahili chenye burudani tele."
    else
        "Profitez de films traduits avec passion en langue Swahili par les DJs les plus populaires d'Afrique."

    fun btnContinue(lang: Language) = if (lang == Language.SW) "Endelea" else "Continuer"
    fun btnSkip(lang: Language) = if (lang == Language.SW) "Ruka" else "Passer"
    fun btnStart(lang: Language) = if (lang == Language.SW) "Anza Sasa" else "Commencer"

    // Bienvenue
    fun welcomeHeading(lang: Language) = if (lang == Language.SW) "Karibu kwenye MOVIE SW" else "Bienvenue sur MOVIE SW"
    fun welcomeSubheading(lang: Language) = if (lang == Language.SW)
        "Filamu na tamthilia zisizo na kikomo kwa Kiswahili na Kifaransa."
    else
        "Films, drames et séries illimités en Swahili et en Français."
    fun btnSignUp(lang: Language) = if (lang == Language.SW) "S'inscrire (Jisajili)" else "S'inscrire"
    fun btnSignIn(lang: Language) = if (lang == Language.SW) "Se connecter (Ingia)" else "Se connecter"
    fun btnAdminAccess(lang: Language) = if (lang == Language.SW) "Ingia kama Admin" else "Connexion Admin"

    // Auth Form
    fun authTitleRegister(lang: Language) = if (lang == Language.SW) "Unda Akaunti Mpya" else "Créer un Nouveau Compte"
    fun authTitleLogin(lang: Language) = if (lang == Language.SW) "Ingia kwenye Akaunti" else "Connexion au Compte"
    fun authAdminTitle(lang: Language) = if (lang == Language.SW) "Lango Kuu la Admin" else "Portail Administrateur"
    fun labelName(lang: Language) = if (lang == Language.SW) "Jina Kamili" else "Nom complet"
    fun labelCountry(lang: Language) = if (lang == Language.SW) "Nchi (mfano Tanzania, Kenya, DRC)" else "Pays (ex. RDC, France, etc.)"
    fun labelEmailOrPhone(lang: Language) = if (lang == Language.SW) "Namba ya Simu au Gmail" else "Numéro de téléphone ou Gmail"
    fun labelPassword(lang: Language) = if (lang == Language.SW) "Nywila (herufi 8 au zaidi)" else "Mot de passe (8+ caractères)"
    fun labelAgeRating(lang: Language) = if (lang == Language.SW) "Chagua Umri Wako" else "Sélectionnez votre âge"
    fun ageOption10(lang: Language) = if (lang == Language.SW) "Miaka 10+ (Familia & Watoto)" else "10+ Ans (Famille & Enfants)"
    fun ageOption18(lang: Language) = if (lang == Language.SW) "Miaka 18+ (Watu Wazima)" else "18+ Ans (Adultes)"
    fun switchHaveAccount(lang: Language) = if (lang == Language.SW) "Tayari una akaunti? Ingia hapa" else "Vous avez déjà un compte ? Se connecter"
    fun switchNeedAccount(lang: Language) = if (lang == Language.SW) "Huna akaunti? Jisajili hapa" else "Pas de compte ? Inscrivez-vous ici"
    fun backToWelcome(lang: Language) = if (lang == Language.SW) "Rudi Bienvenue" else "Retour à Bienvenue"

    // Home & Streaming
    fun featuredBadge(lang: Language) = if (lang == Language.SW) "INAYOVUMA SASA" else "TENDANCE DU MOMENT"
    fun watchNow(lang: Language) = if (lang == Language.SW) "Tazama Sasa" else "Regarder"
    fun addToAlbum(lang: Language) = if (lang == Language.SW) "Weka kwenye Albamu" else "Ajouter à l'Album"
    fun inAlbum(lang: Language) = if (lang == Language.SW) "Ipo kwenye Albamu" else "Dans l'Album"
    fun openDrive(lang: Language) = if (lang == Language.SW) "Fungua kwa Google Drive" else "Ouvrir sur Google Drive"
    fun download(lang: Language) = if (lang == Language.SW) "Pakua kwenye Simu" else "Télécharger sur l'appareil"
    fun downloaded(lang: Language) = if (lang == Language.SW) "Imepakuliwa" else "Téléchargé"
    fun djSectionTitle(lang: Language) = if (lang == Language.SW) "Filamu za Ma-DJ Maarufu" else "Films Traduits par les DJs"
    fun lockVipOnly(lang: Language) = if (lang == Language.SW) "Faili la Admin (Inahitaji VIP)" else "Fichier Admin (Réservé VIP)"
    fun lockedNotice(lang: Language) = if (lang == Language.SW) "Filamu hii ni ya VIP tu! Boresha akaunti yako ili kufungua." else "Ce contenu est réservé aux membres VIP. Passez à VIP pour débloquer."

    // Video Player & Controls
    fun inAppPlayer(lang: Language) = if (lang == Language.SW) "Kicheza cha Ndani ya App" else "Lecteur Intégré"
    fun videoQuality(lang: Language) = if (lang == Language.SW) "Ubora wa Video" else "Qualité Vidéo"
    fun episodes(lang: Language) = if (lang == Language.SW) "Vipindi vya Mfululizo" else "Épisodes de la Série"
    fun commentsTitle(lang: Language) = if (lang == Language.SW) "Maoni ya Watazamaji" else "Commentaires des Spectateurs"
    fun addCommentHint(lang: Language) = if (lang == Language.SW) "Andika maoni yako hapa..." else "Ajoutez votre commentaire ici..."
    fun postComment(lang: Language) = if (lang == Language.SW) "Tuma Maoni" else "Publier"

    // VIP Section
    fun upgradeToVip(lang: Language) = if (lang == Language.SW) "Passer à VIP (Boresha kuwa VIP)" else "Passer à VIP"
    fun currentPlan(lang: Language) = if (lang == Language.SW) "Kiwango Chako Cha Sasa" else "Votre Forfait Actuel"
    fun selectPlan(lang: Language) = if (lang == Language.SW) "Chagua Mpango" else "Choisir ce forfait"
    fun vipPaymentNotice(lang: Language) = if (lang == Language.SW)
        "Kumbuka: Malipo rasmi ya mtandaoni yataunganishwa pindi API itakapokamilika. Unaweza kuboresha hapa kwa ajili ya majaribio ya haraka."
    else
        "Note: L'API de paiement sera intégrée prochainement. Activez un forfait ici en mode essai instantané."

    // Albamu / Library
    fun tabFavorites(lang: Language) = if (lang == Language.SW) "Filamu Zilizohifadhiwa" else "Favoris Enregistrés"
    fun tabDownloads(lang: Language) = if (lang == Language.SW) "Zilizopakuliwa Kwenye Simu" else "Téléchargements Locaux"
    fun downloadQuotaPro(lang: Language, actionLeft: Int, seriesLeft: Int) = if (lang == Language.SW)
        "Baki za Mwezi Huu: Filamu za Action: $actionLeft/10 | Mfululizo: $seriesLeft/5"
    else
        "Quota ce mois : Films d'action : $actionLeft/10 | Séries : $seriesLeft/5"
    fun downloadQuotaUnlimited(lang: Language) = if (lang == Language.SW)
        "Upakuaji Bila Kikomo (VIP Pro Max)"
    else
        "Téléchargements Illimités (VIP Pro Max)"
    fun downloadQuotaFree(lang: Language) = if (lang == Language.SW)
        "Akaunti ya Bure haina upakuaji. Boresha kuwa VIP Pro ili kupakua."
    else
        "Le compte gratuit ne supporte pas le téléchargement. Passez à VIP Pro."

    // Account & Settings
    fun socialFollow(lang: Language) = if (lang == Language.SW) "Tufuate kwenye Mitandao ya Kijamii" else "Suivez-nous sur les Réseaux"
    fun switchLanguage(lang: Language) = if (lang == Language.SW) "Badilisha Lugha" else "Changer de Langue"
    fun logout(lang: Language) = if (lang == Language.SW) "Ondoka kwenye Akaunti" else "Se Déconnecter"

    // Notifications
    fun notificationsTitle(lang: Language) = if (lang == Language.SW) "Arifa na Matangazo" else "Notifications & Annonces"
    fun noNotifications(lang: Language) = if (lang == Language.SW) "Hakuna arifa mpya kwa sasa." else "Aucune nouvelle notification."

    // Admin Dashboard
    fun adminDashboardTitle(lang: Language) = if (lang == Language.SW) "Dashboard ya Admin (Movie SW)" else "Tableau de Bord Admin (Movie SW)"
    fun tabPublish(lang: Language) = if (lang == Language.SW) "Kuchapisha Filamu" else "Publier un Film"
    fun tabManageMovies(lang: Language) = if (lang == Language.SW) "Kusimamia & Kufuta Filamu" else "Gérer & Supprimer Films"
    fun tabCustomers(lang: Language) = if (lang == Language.SW) "Orodha ya Wateja" else "Liste des Clients"
    fun tabStats(lang: Language) = if (lang == Language.SW) "Takwimu za App" else "Statistiques de l'App"
    fun tabSocialAdmin(lang: Language) = if (lang == Language.SW) "Mitandao & Arifa" else "Réseaux & Diffusion"
    fun btnExitAdmin(lang: Language) = if (lang == Language.SW) "Rudi kwenye Mtazamaji" else "Retour Mode Spectateur"
    fun btnDeleteMovie(lang: Language) = if (lang == Language.SW) "Futa Filamu" else "Supprimer le Film"
    fun btnDeleteUser(lang: Language) = if (lang == Language.SW) "Futa Mteja" else "Supprimer le Client"
    fun confirmDeleteMovie(lang: Language, title: String) = if (lang == Language.SW)
        "Una uhakika unataka kuifuta filamu '$title' kutoka kwenye app?"
    else
        "Voulez-vous vraiment supprimer le film '$title' de l'application ?"
    fun confirmDeleteUser(lang: Language, name: String) = if (lang == Language.SW)
        "Una uhakika unataka kumfuta mteja '$name' kutoka kwenye mfumo?"
    else
        "Voulez-vous vraiment supprimer l'utilisateur '$name' du système ?"
}
