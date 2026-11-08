package com.example.util

data class CountryInfo(
    val code: String,     // ISO 2-letter code (e.g. "CD")
    val nameSw: String,   // Swahili name
    val nameFr: String,   // French name
    val dialCode: String  // International dial code (e.g. "+243")
) {
    fun displayName(isSw: Boolean): String = if (isSw) nameSw else nameFr
}

object CountryData {
    // Default country is RD Congo (RDC +243) as requested
    val defaultCountry = CountryInfo(
        code = "CD",
        nameSw = "RD Congo (RDC)",
        nameFr = "RD Congo (RDC)",
        dialCode = "+243"
    )

    val allCountries: List<CountryInfo> = listOf(
        // Priority / Major French & Swahili regions
        CountryInfo("CD", "RD Congo (RDC)", "RD Congo (RDC)", "+243"),
        CountryInfo("TZ", "Tanzania", "Tanzanie", "+255"),
        CountryInfo("KE", "Kenya", "Kenya", "+254"),
        CountryInfo("BI", "Burundi", "Burundi", "+257"),
        CountryInfo("RW", "Rwanda", "Rwanda", "+250"),
        CountryInfo("UG", "Uganda", "Ouganda", "+256"),
        CountryInfo("CG", "Congo Brazzaville", "Congo Brazzaville", "+242"),
        CountryInfo("FR", "Ufaransa", "France", "+33"),
        CountryInfo("BE", "Ubelgiji", "Belgique", "+32"),
        CountryInfo("CA", "Kanada", "Canada", "+1"),
        CountryInfo("US", "Marekani", "États-Unis", "+1"),

        // Other African Nations
        CountryInfo("ZA", "Afrika Kusini", "Afrique du Sud", "+27"),
        CountryInfo("ZM", "Zambia", "Zambie", "+260"),
        CountryInfo("AO", "Angola", "Angola", "+244"),
        CountryInfo("CM", "Kamerun", "Cameroun", "+237"),
        CountryInfo("CI", "Pwani ya Pembe", "Côte d'Ivoire", "+225"),
        CountryInfo("SN", "Senegal", "Sénégal", "+221"),
        CountryInfo("ML", "Mali", "Mali", "+223"),
        CountryInfo("GA", "Gabon", "Gabon", "+241"),
        CountryInfo("GN", "Gine", "Guinée", "+224"),
        CountryInfo("MG", "Madagaska", "Madagascar", "+261"),
        CountryInfo("MZ", "Msumbiji", "Mozambique", "+258"),
        CountryInfo("MW", "Malawi", "Malawi", "+265"),
        CountryInfo("SS", "Sudan Kusini", "Soudan du Sud", "+211"),
        CountryInfo("SD", "Sudan", "Soudan", "+249"),
        CountryInfo("NG", "Nigeria", "Nigéria", "+234"),
        CountryInfo("GH", "Ghana", "Ghana", "+233"),
        CountryInfo("ET", "Ethiopia", "Éthiopie", "+251"),
        CountryInfo("SO", "Somalia", "Somalie", "+252"),
        CountryInfo("EG", "Misri", "Égypte", "+20"),
        CountryInfo("MA", "Moroko", "Maroc", "+212"),
        CountryInfo("DZ", "Aljeria", "Algérie", "+213"),
        CountryInfo("TN", "Tunisia", "Tunisie", "+216"),
        CountryInfo("TG", "Togo", "Togo", "+228"),
        CountryInfo("BJ", "Benin", "Bénin", "+229"),
        CountryInfo("BF", "Burkina Faso", "Burkina Faso", "+226"),
        CountryInfo("NE", "Niger", "Niger", "+227"),
        CountryInfo("TD", "Chad", "Tchad", "+235"),
        CountryInfo("CF", "Afrika ya Kati", "Centrafrique", "+236"),
        CountryInfo("KM", "Komoro", "Comores", "+269"),
        CountryInfo("DJ", "Jibuti", "Djibouti", "+253"),
        CountryInfo("ZW", "Zimbabwe", "Zimbabwe", "+263"),
        CountryInfo("BW", "Botswana", "Botswana", "+267"),
        CountryInfo("NA", "Namibia", "Namibie", "+264"),

        // Global Countries
        CountryInfo("GB", "Uingereza", "Royaume-Uni", "+44"),
        CountryInfo("DE", "Ujerumani", "Allemagne", "+49"),
        CountryInfo("IT", "Italia", "Italie", "+39"),
        CountryInfo("ES", "Uhispania", "Espagne", "+34"),
        CountryInfo("CH", "Uswisi", "Suisse", "+41"),
        CountryInfo("NL", "Uholanzi", "Pays-Bas", "+31"),
        CountryInfo("SE", "Uswidi", "Suède", "+46"),
        CountryInfo("NO", "Norwe", "Norvège", "+47"),
        CountryInfo("TR", "Uturuki", "Turquie", "+90"),
        CountryInfo("RU", "Urusi", "Russie", "+7"),
        CountryInfo("CN", "China", "Chine", "+86"),
        CountryInfo("KR", "Korea Kusini", "Corée du Sud", "+82"),
        CountryInfo("JP", "Japani", "Japon", "+81"),
        CountryInfo("IN", "Uhindi", "Inde", "+91"),
        CountryInfo("AE", "Falme za Kiarabu (Dubai)", "Émirats Arabes Unis", "+971"),
        CountryInfo("SA", "Saudi Arabia", "Arabie Saoudite", "+966"),
        CountryInfo("QA", "Qatar", "Qatar", "+974"),
        CountryInfo("BR", "Brazili", "Brésil", "+55"),
        CountryInfo("AU", "Australia", "Australie", "+61")
    )
}
