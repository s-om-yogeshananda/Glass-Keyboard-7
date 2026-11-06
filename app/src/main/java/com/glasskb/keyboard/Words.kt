package com.glasskb.keyboard

/** অফলাইন বিল্ট-ইন শব্দ তালিকা (বেশি ব্যবহৃত শব্দ আগে)। এছাড়া কীবোর্ড আপনার টাইপ থেকে নতুন শব্দ শেখে। */
object Words {

    private const val EN_TEXT = """
the be to of and a in that have it for not on with he as you do at this but his by from they we say her she or an will my one all would there their what so up out if about who get which go me when make can like time no just him know take people into year your good some could them see other than then now look only come its over think also back after use two how our work first well way even new want because any these give day most us
is are was were been has had did does am being very more much many such own same few those here where why again off down should still between must might shall may through during before under never each both while last long great little right too old different next large small high every another found away home life world school state family student group country problem hand part place case week company system program question government number night point city area money story fact month lot book eye job word business issue side kind head house service friend father power hour game line end member law car community name president team minute idea kid body information face others level office door health person art war history party result change morning reason research girl guy moment air teacher force education
hello hi hey thanks thank please sorry yes okay ok sure maybe welcome bad nice awesome love happy sad angry tired hungry busy free ready late early today tomorrow yesterday tonight soon later always often sometimes already almost enough really actually probably definitely exactly anyway together without within against around across along behind beyond inside outside
call ask need feel become leave put mean keep let begin seem help talk turn start show hear play run move live believe hold bring happen write provide sit stand lose pay meet include continue set learn lead understand watch follow stop create speak read allow add spend grow open walk win offer remember consider appear buy wait serve die send expect build stay fall cut reach kill remain suggest raise pass sell require report decide pull
phone message email mobile internet online video photo picture music song movie food water coffee tea lunch dinner breakfast market shop store price meeting class exam test study college university brother sister mother son daughter husband wife baby
whose whom whatever whenever wherever however therefore although though unless until since whether either neither nor
monday tuesday wednesday thursday friday saturday sunday january february march april june july august september october november december
bangladesh dhaka india pakistan america english bengali assalamualaikum inshallah mashallah alhamdulillah
i'm i'll i've i'd don't can't won't it's that's what's you're we're they're isn't doesn't didn't couldn't wouldn't shouldn't let's there's here's he's she's you'll we'll you've we've aren't wasn't haven't hasn't
birthday congratulations wonderful beautiful amazing important possible available interested information problem question answer example together everything something nothing everyone someone anything everybody
"""

    private const val BN_TEXT = """
আমি আমার আমাকে আমরা আমাদের তুমি তোমার তোমাকে তোমরা তোমাদের আপনি আপনার আপনাকে আপনারা সে তার তাকে তারা তাদের এই এটা এটি ওটা যে যা যার কি কী কে কেন কোথায় কোথা কখন কবে কিভাবে কেমন কত কোন কিছু কেউ সবাই সব সবার একটা একটি একজন দুই তিন চার পাঁচ ছয় সাত আট নয় দশ
আছি আছো আছেন আছে ছিল ছিলাম থাকা থাকি থাকো থাকেন হয় হয়েছে হবে হলো হচ্ছে হতে হই হও করি করো করে করেছি করেছে করবো করবে করছি করছো করছেন করা করতে দেখি দেখো দেখা দেখছি বলি বলো বলে বলেছে বলবো বলা বলতে যাই যাও যায় যাবো যাচ্ছি যাচ্ছো যাওয়া আসি আসো আসে আসবো আসছি আসছো আসা আসতে খাই খাও খেয়েছি খাওয়া খাবার পড়ি পড়া পড়ছি লিখি লেখা লিখছি শুনি শুনো শোনা শুনছি জানি জানো জানা জানাবো জানতে পারি পারো পারবো পারবে পারছি চাই চাও চেষ্টা দাও দিন দিয়েছি দিচ্ছি নাও নিয়ে নিচ্ছি নেই নাই না হ্যাঁ জি ঠিক
ভালো ভালোবাসি ভালোবাসা খারাপ সুন্দর বড় ছোট নতুন পুরানো অনেক একটু খুব আরও আর আবার এখন তখন আজ কাল গতকাল আগামীকাল আজকে কালকে সকাল দুপুর বিকাল সন্ধ্যা রাত রাতে সকালে বিকালে আগে পরে পর সাথে সঙ্গে জন্য থেকে পর্যন্ত মধ্যে ভিতরে বাইরে উপর নিচে কাছে দূরে এখানে সেখানে ওখানে কোথাও
বাসা বাড়ি স্কুল কলেজ অফিস বাজার দোকান রাস্তা গাড়ি বাস ট্রেন সময় টাকা কাজ কথা নাম বন্ধু ভাই বোন আম্মু আব্বু মা বাবা ভাইয়া আপু দাদা দাদী নানা নানী চাচা চাচী মামা খালা ফুফু স্বামী স্ত্রী ছেলে মেয়ে বাচ্চা মানুষ লোক শিক্ষক ছাত্র ডাক্তার
ধন্যবাদ শুভেচ্ছা অভিনন্দন স্বাগতম দুঃখিত মাফ ক্ষমা সালাম আসসালামু আলাইকুম ওয়ালাইকুম বিসমিল্লাহ ইনশাআল্লাহ মাশাআল্লাহ আলহামদুলিল্লাহ আল্লাহ সুপ্রভাত শুভ রাত্রি জন্মদিন ঈদ মোবারক
পানি চা কফি ভাত রুটি মাছ মাংস ডাল সবজি ফল দুধ ডিম ফোন মোবাইল ইন্টারনেট মেসেজ ফেসবুক ছবি ভিডিও গান সিনেমা খেলা ক্রিকেট ফুটবল
বাংলাদেশ ঢাকা চট্টগ্রাম সিলেট রাজশাহী খুলনা বরিশাল রংপুর ময়মনসিংহ বাংলা ইংরেজি ভাষা দেশ বিশ্ব জীবন স্বপ্ন আশা মন মনে হৃদয় চোখ হাত পা মাথা শরীর স্বাস্থ্য অসুস্থ সুস্থ ব্যথা ঘুম ক্ষুধা আনন্দ খুশি দুঃখ কষ্ট রাগ ভয় চিন্তা সমস্যা সমাধান প্রশ্ন উত্তর সত্যি মিথ্যা সত্য ভুল সঠিক দরকার প্রয়োজন সাহায্য অপেক্ষা শুরু শেষ তাহলে কিন্তু এবং অথবা বা যদি তবে কারণ তাই সুতরাং অবশ্যই হয়তো
"""

    private const val HI_TEXT = """
मैं मेरा मेरी मेरे मुझे हम हमारा हमारी हमारे हमें तुम तुम्हारा तुम्हारी तुम्हारे तुम्हें आप आपका आपकी आपके आपको वह उसका उसकी उसके उसे वे उनका उनकी उनके उन्हें यह इसका इसकी इसके इसे ये इनका
है हैं था थी थे हूँ हो होगा होगी होंगे होता होती होते हुआ हुई हुए रहा रही रहे गया गई गए गयी जाता जाती जाते जाना आना आया आई आए करना करता करती करते किया की किए करो करें करूँगा करूँगी
क्या कौन कहाँ कब कैसे कैसा कैसी क्यों कितना कितनी कितने कौनसा कोई कुछ सब सभी बहुत थोड़ा ज़्यादा ज्यादा कम और या लेकिन मगर पर परन्तु क्योंकि अगर तो तब जब तक से में को का के की ने पर भी ही
नमस्ते नमस्कार धन्यवाद शुक्रिया स्वागत माफ़ माफी क्षमा कृपया अच्छा अच्छी अच्छे बुरा बुरी बुरे सुंदर सुन्दर बड़ा बड़ी बड़े छोटा छोटी छोटे नया नई नए पुराना पुरानी पुराने ठीक सही गलत हाँ नहीं ना जी
आज कल अभी अब फिर पहले बाद सुबह दोपहर शाम रात दिन हफ़्ता महीना साल समय वक़्त घंटा मिनट
घर परिवार माँ पिता पिताजी भाई बहन दादा दादी नाना नानी चाचा चाची मामा मामी बेटा बेटी पति पत्नी दोस्त मित्र लड़का लड़की आदमी औरत बच्चा बच्चे लोग
पानी चाय कॉफ़ी खाना रोटी चावल दाल सब्ज़ी फल दूध अंडा मछली मांस मिठाई नाश्ता भोजन भूख प्यास
स्कूल कॉलेज दफ़्तर बाज़ार दुकान सड़क गाड़ी बस रेल ट्रेन हवाई जहाज़ स्टेशन शहर गाँव देश दुनिया
फ़ोन मोबाइल इंटरनेट मैसेज संदेश फ़ोटो तस्वीर वीडियो गाना गीत फ़िल्म खेल क्रिकेट फ़ुटबॉल
भारत हिंदुस्तान हिन्दी हिंदी अंग्रेज़ी भाषा दिल्ली मुंबई कोलकाता चेन्नई बंगाल बांग्लादेश ढाका पाकिस्तान नेपाल
प्यार मोहब्बत ख़ुशी खुशी दुख सुख जीवन ज़िंदगी सपना आशा मन दिल आँख हाथ पैर सिर शरीर स्वास्थ्य बीमार
काम बात नाम पैसा रुपया मदद सवाल जवाब बात समझ मालूम पता जानना देखना सुनना बोलना कहना पढ़ना लिखना खाना पीना सोना उठना बैठना चलना दौड़ना
शुभ जन्मदिन बधाई शुभकामनाएँ शुभकामनाएं मुबारक त्योहार दिवाली होली ईद राखी
आपका आपसे मुझसे तुमसे उससे हमसे इससे किससे इसलिए इसीलिए जैसे वैसे ऐसे वैसा ऐसा जैसा कैसे
"""

    val en: List<String> by lazy { EN_TEXT.split(' ', '\n').filter { it.isNotEmpty() }.distinct() }
    val bn: List<String> by lazy { BN_TEXT.split(' ', '\n').filter { it.isNotEmpty() }.distinct() }
    val hi: List<String> by lazy { HI_TEXT.split(' ', '\n').filter { it.isNotEmpty() }.distinct() }

    /** শব্দের পরে সাধারণত যা আসে (প্রাথমিক অনুমান) */
    val enNext: Map<String, List<String>> = mapOf(
        "i" to listOf("am", "have", "will", "can", "think", "want", "love", "need"),
        "i'm" to listOf("not", "going", "so", "fine", "sorry"),
        "you" to listOf("are", "can", "have", "will", "know", "should"),
        "we" to listOf("are", "can", "will", "have", "need"),
        "they" to listOf("are", "have", "will", "were"),
        "he" to listOf("is", "was", "has", "will"),
        "she" to listOf("is", "was", "has", "will"),
        "it" to listOf("is", "was", "will", "has"),
        "is" to listOf("it", "the", "a", "not", "this"),
        "are" to listOf("you", "the", "we", "not", "they"),
        "the" to listOf("best", "same", "first", "most", "following"),
        "a" to listOf("lot", "little", "few", "good", "great"),
        "to" to listOf("be", "the", "do", "get", "go", "see"),
        "of" to listOf("the", "course", "my", "a"),
        "in" to listOf("the", "a", "my", "this"),
        "for" to listOf("the", "me", "you", "a", "your"),
        "on" to listOf("the", "my", "a", "your"),
        "with" to listOf("you", "the", "me", "a", "my"),
        "thank" to listOf("you", "god"),
        "thanks" to listOf("for", "a", "so"),
        "good" to listOf("morning", "night", "evening", "afternoon", "luck", "job"),
        "how" to listOf("are", "is", "to", "much", "many", "do"),
        "what" to listOf("is", "are", "do", "about", "time"),
        "where" to listOf("are", "is", "do", "can"),
        "when" to listOf("will", "do", "is", "you"),
        "why" to listOf("are", "is", "do", "not"),
        "can" to listOf("you", "i", "we", "not"),
        "will" to listOf("be", "you", "do", "not"),
        "do" to listOf("you", "not", "it", "we", "i"),
        "don't" to listOf("know", "worry", "think", "have", "want"),
        "have" to listOf("a", "to", "you", "been", "the"),
        "see" to listOf("you", "the", "it", "this"),
        "let" to listOf("me", "us", "it", "them"),
        "let's" to listOf("go", "do", "see", "meet", "talk"),
        "please" to listOf("help", "let", "send", "call", "tell"),
        "happy" to listOf("birthday", "new", "to", "eid"),
        "hello" to listOf("there", "sir", "everyone", "friend"),
        "hi" to listOf("there", "how", "all", "sir"),
        "very" to listOf("good", "much", "well", "nice", "happy"),
        "my" to listOf("name", "friend", "love", "dear", "phone"),
        "your" to listOf("name", "help", "time", "phone", "message"),
        "not" to listOf("sure", "a", "yet", "now", "possible"),
        "no" to listOf("problem", "worries", "one", "way"),
        "see you" to listOf("soon", "later", "tomorrow"),
        "i'll" to listOf("be", "call", "see", "try", "do")
    )

    val bnNext: Map<String, List<String>> = mapOf(
        "আমি" to listOf("আছি", "ভালো", "তোমাকে", "যাচ্ছি", "জানি"),
        "তুমি" to listOf("কেমন", "কি", "কোথায়", "আছো", "কী"),
        "আপনি" to listOf("কেমন", "কি", "কোথায়", "কী"),
        "কেমন" to listOf("আছো", "আছেন", "আছি", "লাগছে"),
        "ধন্যবাদ" to listOf("আপনাকে", "তোমাকে", "সবাইকে"),
        "আসসালামু" to listOf("আলাইকুম"),
        "ওয়ালাইকুম" to listOf("আসসালাম"),
        "শুভ" to listOf("সকাল", "রাত্রি", "জন্মদিন", "সন্ধ্যা", "দিন"),
        "সুপ্রভাত" to listOf("সবাইকে"),
        "ভালো" to listOf("আছি", "থাকো", "থাকবেন", "লাগছে", "হয়েছে"),
        "কি" to listOf("করছো", "খবর", "করছেন", "হয়েছে"),
        "কী" to listOf("করছো", "খবর", "করছেন", "হয়েছে"),
        "আজ" to listOf("সকালে", "রাতে", "কি", "আমি"),
        "না" to listOf("আমি", "ভাই", "জানি", "পারবো"),
        "হ্যাঁ" to listOf("ঠিক", "আমি", "অবশ্যই"),
        "জন্মদিনের" to listOf("শুভেচ্ছা"),
        "ইনশাআল্লাহ" to listOf("আমি", "হবে"),
        "অনেক" to listOf("ভালো", "ধন্যবাদ", "দিন", "সুন্দর"),
        "খুব" to listOf("ভালো", "সুন্দর", "খারাপ", "দরকার"),
        "আপনার" to listOf("নাম", "কি", "সাহায্য"),
        "তোমার" to listOf("নাম", "কি", "খবর")
    )

    val hiNext: Map<String, List<String>> = mapOf(
        "मैं" to listOf("ठीक", "आपसे", "यहाँ", "अभी", "आज"),
        "आप" to listOf("कैसे", "कहाँ", "क्या", "कौन", "ठीक"),
        "तुम" to listOf("कैसे", "कहाँ", "क्या", "कब", "ठीक"),
        "क्या" to listOf("हाल", "हो", "आप", "तुम", "कर"),
        "कैसे" to listOf("हैं", "हो", "करें", "रहे"),
        "कैसा" to listOf("लग", "है", "रहा"),
        "नमस्ते" to listOf("सर", "दोस्तों", "जी", "भाई"),
        "धन्यवाद" to listOf("आपका", "आपको", "सभी", "जी"),
        "शुभ" to listOf("प्रभात", "रात्रि", "जन्मदिन", "दिन", "संध्या"),
        "जन्मदिन" to listOf("मुबारक", "की", "पर"),
        "बहुत" to listOf("अच्छा", "सुंदर", "धन्यवाद", "ज़्यादा"),
        "यह" to listOf("है", "बहुत", "क्या", "सही"),
        "वह" to listOf("है", "कहाँ", "क्या", "भी"),
        "हम" to listOf("सब", "लोग", "आपके", "भी"),
        "मेरा" to listOf("नाम", "घर", "फ़ोन", "दोस्त"),
        "मेरी" to listOf("बात", "माँ", "तरफ़", "जान"),
        "आपका" to listOf("नाम", "स्वागत", "धन्यवाद", "क्या"),
        "आपकी" to listOf("मदद", "बात", "क्या", "तरफ़"),
        "ठीक" to listOf("है", "हूँ", "हैं", "से"),
        "नहीं" to listOf("है", "पता", "जानता", "हो"),
        "हाँ" to listOf("जी", "ठीक", "मैं", "बिल्कुल"),
        "कृपया" to listOf("मदद", "बताइए", "ध्यान", "प्रतीक्षा")
    )

    val enStart = listOf("I", "The", "Hi")
    val bnStart = listOf("আমি", "আপনি", "ধন্যবাদ")
    val hiStart = listOf("मैं", "आप", "धन्यवाद")
}
