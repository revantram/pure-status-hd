# 🎬 PureStatus HD — WhatsApp Status HD Video Compressor & Splitter

> **100% Ad-Free • 100% Offline • iOS-Inspired Liquid Glass UI • True OLED Black (#000000)**  
> **डेवलपर:** रेवंतराम भाटी (Revantram Bhati) | 📞 **+91 8696663615**

---

## ✨ विशेषताएं (Key Features)

1. **WhatsApp स्टेटस के लिए क्रिस्टल क्लियर विज़ुअल क्वालिटी:**
   - WhatsApp स्टेटस 720p से ऊपर और भारी बिटरेट वाले वीडियो को अपने सर्वर पर खराब अल्गोरिदम से कंप्रेस करता है जिससे वीडियो पिक्सलेट और धुंधला दिखता है।
   - **PureStatus HD** आपके वीडियो को WhatsApp स्टेटस के सबसे अनुकूल मानकों (720p 30fps CFR, AVC/H.264 High Profile, 1s GOP Keyframes) पर ऑप्टिमाइज़ करता है। जिससे फाइल साइज 70-85% घट जाता है और WhatsApp इसे बिना धुंधला किए हूबहू फुल HD में लगाता है।

2. **30-सेकंड ऑटो स्टेटस स्प्लिटर (Seamless Splitter):**
   - 30 सेकंड से लंबे वीडियो को बिना क्वालिटी गिराए लगातार पार्ट 1, पार्ट 2 आदि में ऑटोमैटिकली विभाजित करता है।
   - एक क्लिक में सभी पार्ट्स को क्रम से WhatsApp स्टेटस पर शेयर करने की सुविधा।

3. **अल्ट्रा-फास्ट हार्डवेयर एक्सेलेरेशन (Android MediaCodec):**
   - किसी भारी FFmpeg बाइनरी के बिना सीधे डिवाइस के हार्डवेयर एनकोडर (Qualcomm Adreno, MediaTek, Samsung Exynos, Tensor) का उपयोग करता है।
   - 30-सेकंड का वीडियो मात्र 2-4 सेकंड में तैयार होता है, बिना बैटरी खपत या फोन गर्म हुए।
   - APK साइज 3-5 MB के भीतर रहता है।

4. **प्रीमियम iOS लिक्विड ग्लास (Liquid Glass) व ट्रू OLED ब्लैक डिज़ाइन:**
   - **Night Mode:** ट्रू OLED ब्लैक (`#000000`) बैकग्राउंड, डीप ग्लास कार्ड्स और निऑन एमराल्ड एक्सेंट।
   - **Day Mode:** ट्रांसलूसेंट फ्रॉस्टेड ग्लास, क्लीन शैडोज़ और हाई-कॉन्ट्रास्ट लेआउट।
   - **नेविगेशन:** कोई बॉटम बार नहीं! टॉप-राइट में वन-टैप सन/मून थीम टॉगल और सेटिंग्स बटन।

5. **इन-ऐप डायरेक्ट अपडेट (In-App Update):**
   - सेटिंग्स में एक क्लिक में नया GitHub रिलीज़ चेक करें, ऐप के अंदर ही APK डाउनलोड करें और सीधे इंस्टॉल करें।
   - दोस्तों को एक क्लिक में WhatsApp पर ऐप का APK शेयर करने का बटन।

---

## 🛠️ तकनीकी संरचना (Tech Stack)

- **भाषा:** Kotlin 2.0
- **UI Framework:** Jetpack Compose (Material 3 + Liquid Glass Custom Components)
- **वीडियो इंजन:** AndroidX Media3 Transformer & Hardware MediaCodec
- **इमेज/थंबनेल लोडर:** Coil Compose
- **टारगेट SDK:** Android 15 (API 35) | **Min SDK:** Android 7.0 (API 24)
- **CI/CD:** GitHub Actions ऑटोमेटेड APK बिल्ड व रिलीज़

---

## 🚀 GitHub Actions से APK बिल्ड करना

यह प्रोजेक्ट सीधे GitHub Actions से बिल्ड होने के लिए कॉन्फ़िगर किया गया है:
1. कोड को GitHub पर पुश करें।
2. `.github/workflows/build-apk.yml` अपने आप Ubuntu रनर पर JDK 17 और Android SDK सेटअप करके APK तैयार करेगा।
3. GitHub Actions टैब से **PureStatusHD-APK** डाउनलोड करें या Release टैग (`v1.0.0`) बनाकर सीधे APK रिलीज़ प्राप्त करें।

---

## 👤 डेवलपर संपर्क (Developer Contact)

- **डेवलपर:** रेवंतराम भाटी (Revantram Bhati)
- **मोबाइल / व्हाट्सएप:** [+91 8696663615](tel:+918696663615)
- **GitHub:** [revantram](https://github.com/revantram)
- **लाइसेंस:** ओपन-सोर्स (MIT)
