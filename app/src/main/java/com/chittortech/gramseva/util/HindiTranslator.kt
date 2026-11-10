package com.chittortech.gramseva.util

object HindiTranslator {

    private val commodityMap = mapOf(
        "bitter gourd" to "करेला",
        "bottle gourd" to "लौकी (घिया)",
        "bhindi(ladies finger)" to "भिंडी",
        "ladies finger" to "भिंडी",
        "okra" to "भिंडी",
        "brinjal" to "बैंगन",
        "eggplant" to "बैंगन",
        "cabbage" to "पत्तागोभी",
        "cauliflower" to "फूलगोभी",
        "tomato" to "टमाटर",
        "potato" to "आलू",
        "onion" to "प्याज",
        "garlic" to "लहसुन",
        "ginger(green)" to "अदरक",
        "ginger" to "अदरक",
        "green chilli" to "हरी मिर्च",
        "chilli" to "मिर्च",
        "coriander" to "धनिया",
        "coriander(leaves)" to "हरा धनिया",
        "methi" to "मेथी",
        "fenugreek" to "मेथी",
        "fenugreek(leaves)" to "हरी मेथी",
        "mustard" to "सरसों",
        "soyabean" to "सोयाबीन",
        "soybean" to "सोयाबीन",
        "wheat" to "गेहूं",
        "maize" to "मक्का",
        "barley" to "जौ",
        "bajra(pearl millet/cumbu)" to "बाजरा",
        "bajra" to "बाजरा",
        "pearl millet" to "बाजरा",
        "jowar" to "ज्वार",
        "sorghum" to "ज्वार",
        "paddy(basmati)" to "बासमती धान",
        "paddy(dhan)" to "धान",
        "paddy" to "धान",
        "rice" to "चावल",
        "gram" to "चना",
        "bengal gram" to "चना",
        "chana" to "चना",
        "moong" to "मूंग दाल",
        "green gram" to "मूंग दाल",
        "urad" to "उड़द दाल",
        "black gram" to "उड़द दाल",
        "arhar" to "अरहर",
        "tur" to "अरहर",
        "pigeon pea" to "अरहर",
        "lentil" to "मसूर",
        "masur" to "मसूर",
        "groundnut" to "मूंगफली",
        "peanut" to "मूंगफली",
        "cotton" to "कपास",
        "guar" to "ग्वार",
        "cluster bean" to "ग्वार फली",
        "cluster beans" to "ग्वार फली",
        "gwar" to "ग्वार",
        "cumin" to "जीरा",
        "jeera" to "जीरा",
        "sesamum" to "तिल",
        "sesame" to "तिल",
        "til" to "तिल",
        "castor" to "अरंडी",
        "isabgol" to "इसबगोल",
        "turmeric" to "हल्दी",
        "fennel" to "सौंफ",
        "apple" to "सेब",
        "banana - green" to "कच्चा केला",
        "banana" to "केला",
        "mango" to "आम",
        "orange" to "संतरा",
        "pomegranate" to "अनार",
        "grapes" to "अंगूर",
        "guava" to "अमरूद",
        "papaya" to "पपीता",
        "watermelon" to "तरबूज",
        "muskmelon" to "खरबूजा",
        "lemon" to "नींबू",
        "lime" to "नींबू",
        "carrot" to "गाजर",
        "radish" to "मूली",
        "spinach" to "पालक",
        "peas" to "हरी मटर",
        "green peas" to "हरी मटर",
        "pumpkin" to "कद्दू",
        "cucumber" to "खीरा",
        "beetroot" to "चुकंदर",
        "colocasia" to "अरबी",
        "arbi" to "अरबी",
        "elephant yam(suran)/ amorphophallus" to "सूरन (जिमीकंद)",
        "elephant yam(suran)" to "सूरन (जिमीकंद)",
        "amorphophallus" to "सूरन (जिमीकंद)",
        "yam" to "जिमीकंद",
        "indian beans(seam)" to "सेम फली",
        "indian beans" to "सेम फली",
        "seam" to "सेम फली",
        "french beans(frasbean)" to "फ्रासबीन",
        "french beans" to "फ्रासबीन",
        "beans" to "फलियां",
        "cowpea(veg)" to "लोबिया (चौलाई फली)",
        "cowpea" to "लोबिया",
        "drumstick" to "सहजन (मोरिंगा)",
        "mint(pudina)" to "पुदीना",
        "mint" to "पुदीना",
        "peas wet" to "हरी मटर",
        "sweet corn" to "स्वीट कॉर्न",
        "capsicum" to "शिमला मिर्च",
        "sweet potato" to "शकरकंद",
        "mushroom" to "मशरूम",
        "ridgegourd(tori)" to "तोरई",
        "ridge gourd" to "तोरई",
        "sponge gourd" to "गिलकी (तोरई)",
        "snakegourd" to "चिचिंडा",
        "pointed gourd(parval)" to "परवल",
        "parval" to "परवल",
        "tinda" to "टिंडा",
        "ashgourd" to "पेठा (कुम्हड़ा)",
        "clove" to "लौंग",
        "cardamom" to "इलायची",
        "tobacco" to "तंबाकू"
    )

    private val stateMap = mapOf(
        "rajasthan" to "राजस्थान",
        "madhya pradesh" to "मध्य प्रदेश",
        "uttar pradesh" to "उत्तर प्रदेश",
        "gujarat" to "गुजरात",
        "punjab" to "पंजाब",
        "haryana" to "हरियाणा",
        "maharashtra" to "महाराष्ट्र",
        "bihar" to "बिहार",
        "keralam" to "केरल",
        "kerala" to "केरल",
        "odisha" to "ओडिशा",
        "orissa" to "ओडिशा",
        "west bengal" to "पश्चिम बंगाल",
        "karnataka" to "कर्नाटक",
        "tamil nadu" to "तमिलनाडु",
        "andhra pradesh" to "आंध्र प्रदेश",
        "telangana" to "तेलंगाना",
        "himachal pradesh" to "हिमाचल प्रदेश",
        "uttarakhand" to "उत्तराखंड",
        "chhattisgarh" to "छत्तीसगढ़",
        "jharkhand" to "झारखंड",
        "delhi" to "दिल्ली",
        "assam" to "असम"
    )

    private val districtMap = mapOf(
        "chittorgarh" to "चित्तौड़गढ़",
        "chittorgarh(rajasthan)" to "चित्तौड़गढ़",
        "nimbahera" to "निम्बाहेड़ा",
        "kapasan" to "कपासन",
        "begun" to "बेगूं",
        "begu" to "बेगूं",
        "neemuch" to "नीमच",
        "mandsaur" to "मंदसौर",
        "kota" to "कोटा",
        "jaipur" to "जयपुर",
        "jodhpur" to "जोधपुर",
        "udaipur" to "उदयपुर",
        "bikaner" to "बीकानेर",
        "ganganagar" to "श्रीगंगानगर",
        "sri ganganagar" to "श्रीगंगानगर",
        "jalore" to "जालोर",
        "suratgarh" to "सूरतगढ़",
        "kozhikode(calicut)" to "कोझिकोड",
        "kozhikode" to "कोझिकोड",
        "piravam" to "पिरावोम",
        "alappuzha" to "अलप्पुझा",
        "ernakulam" to "एर्नाकुलम",
        "rayagada" to "रायगड़ा",
        "indore" to "इंदौर",
        "bhopal" to "भोपाल",
        "ujjain" to "उज्जैन",
        "ratlam" to "रतलाम",
        "dewas" to "देवास",
        "sagar" to "सागर",
        "banda" to "बांदा",
        "etah" to "एटा",
        "auraiya" to "औरैया",
        "lucknow" to "लखनऊ",
        "kanpur" to "कानपुर",
        "varanasi" to "वाराणसी",
        "agra" to "आगरा",
        "meerut" to "मेरठ",
        "mehsana" to "मेहसाणा",
        "rajkot" to "राजकोट",
        "ahmedabad" to "अहमदाबाद",
        "surat" to "सूरत",
        "nashik" to "नासिक",
        "pune" to "पुणे",
        "nagpur" to "नागपुर",
        "ludhiana" to "लुधियाना",
        "amritsar" to "अमृतसर",
        "karnal" to "करनाल",
        "hisar" to "हिसार",
        "sirsa" to "सिरसा",
        "patna" to "पटना"
    )

    private val varietyMap = mapOf(
        "general" to "सामान्य देसी",
        "desi" to "देसी",
        "hybrid" to "हाइब्रिड",
        "local" to "स्थानीय देसी",
        "american" to "अमेरिकन कॉटन",
        "gwar" to "देसी ग्वार",
        "bitter gourd" to "लंबा हरा",
        "bottle gourd" to "गोल व लंबा",
        "cabbage" to "पत्तागोभी देसी",
        "bhindi" to "हरी मुलायम",
        "1509" to "1509 बासमती",
        "1121" to "1121 बासमती",
        "super" to "सुपर क्वालिटी",
        "faq" to "मानक",
        "banana - green" to "कच्चा हरा केला",
        "amorphophallus" to "देसी सूरन"
    )

    fun translateCommodity(raw: String): String {
        val clean = raw.trim().lowercase()
        // Direct match
        commodityMap[clean]?.let { return it }

        // Partial match
        for ((key, hindi) in commodityMap) {
            if (clean.contains(key)) return hindi
        }
        return raw.trim()
    }

    fun translateState(raw: String): String {
        val clean = raw.trim().lowercase()
        return stateMap[clean] ?: raw.trim()
    }

    fun translateDistrict(raw: String): String {
        val clean = raw.trim().lowercase()
        return districtMap[clean] ?: raw.trim()
    }

    fun translateMarket(raw: String): String {
        var clean = raw.trim()
        val lower = clean.lowercase()

        // Replace English keywords
        clean = clean
            .replace("APMC", "मंडी", ignoreCase = true)
            .replace("Market", "मंडी", ignoreCase = true)
            .replace("Yard", "मंडी यार्ड", ignoreCase = true)
            .replace("(F&V)", "(फल व सब्जी)", ignoreCase = true)
            .trim()

        // Check if market name contains known towns
        districtMap.forEach { (eng, hi) ->
            if (lower.contains(eng)) {
                clean = clean.replace(Regex("(?i)$eng"), hi)
            }
        }

        if (!clean.contains("मंडी")) {
            clean = "$clean मंडी"
        }
        return clean
    }

    fun translateVariety(raw: String, commodity: String = ""): String {
        val clean = raw.trim().lowercase()
        if (clean.isBlank() || clean == "other" || clean == "general" || clean == "local" || clean == commodity.trim().lowercase()) {
            return "स्थानीय देसी"
        }
        varietyMap[clean]?.let { return it }
        for ((key, hindi) in varietyMap) {
            if (clean.contains(key)) return hindi
        }
        return raw.trim()
    }

    fun translateGrade(raw: String): String {
        val clean = raw.trim().uppercase()
        return when {
            clean.contains("GRADE A") -> "उत्तम (A ग्रेड)"
            clean.contains("GRADE B") -> "मध्यम (B ग्रेड)"
            clean.contains("GRADE C") -> "सामान्य (C ग्रेड)"
            clean == "FAQ" -> "मानक (FAQ)"
            clean == "LOCAL" -> "स्थानीय देसी"
            clean == "MEDIUM" -> "मध्यम दाना"
            clean == "LARGE" -> "बोल्ड दाना"
            clean == "SMALL" -> "बारीक दाना"
            clean == "OTHER" || clean.isBlank() -> "मानक (FAQ)"
            else -> raw.trim()
        }
    }

    fun translateTrend(trend: String): String {
        return when {
            trend.contains("तेज", ignoreCase = true) -> "तेज ↑"
            trend.contains("मंदा", ignoreCase = true) -> "मंदा ↓"
            trend.contains("स्थिर", ignoreCase = true) -> "स्थिर ↔"
            trend.contains("BULLISH", ignoreCase = true) -> "तेज ↑"
            trend.contains("BEARISH", ignoreCase = true) -> "मंदा ↓"
            else -> "स्थिर ↔"
        }
    }
}

