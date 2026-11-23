# TV Pad & TV Arcade (দুটি অফলাইন ব্লুটুথ টিভি গেমিং অ্যাপ)

এই প্রজেক্টে দুটি আলাদা অ্যান্ড্রয়েড অ্যাপ রয়েছে যা ইন্টারনেট ছাড়াই ব্লুটুথের মাধ্যমে একসাথে কাজ করে:

1. **`TV-Pad-Phone.apk` (ফোনের অ্যাপ — `:app`)**
   - ফোনকে আসল ব্লুটুথ HID গেমপ্যাড ও টিভি রিমোটে রূপান্তর করে।
   - ডুয়াল অ্যানালগ স্টিক, ৮-মুখী D-Pad, A/B/X/Y, L1/L2/R1/R2, Select/Start/Home এবং টিভি রিমোট কন্ট্রোল।

2. **`TV-Arcade-TV.apk` (টিভির অ্যাপ — `:tv`)**
   - Android TV / Google TV-এর জন্য তৈরি অফলাইন আর্কেড গেম প্যাক।
   - রয়েছে **Space Striker**, **Neon Snake**, **Brick Breaker** এবং একটি **Live Gamepad Input Tester**।

---

## GitHub থেকে দুটি অ্যাপ ডাউনলোড ও ইনস্টল করার নিয়ম

1. AI Studio-এর Settings মেনু থেকে প্রজেক্টটি **GitHub-এ Push** করুন।
2. আপনার GitHub রিপোজিটরির **Actions** ট্যাবে গেলে স্বয়ংক্রিয়ভাবে **"Build Phone & TV APKs"** রান হয়ে যাবে।
3. বিল্ড শেষ হলে রিপোজিটরির **Releases** (অথবা Actions Artifact) থেকে দুটি APK ডাউনলোড করুন:
   - `TV-Pad-Phone.apk` → আপনার **অ্যান্ড্রয়েড ফোনে** ইনস্টল করুন।
   - `TV-Arcade-TV.apk` → আপনার **অ্যান্ড্রয়েড টিভিতে** (USB পেনড্রাইভ বা Send Files to TV অ্যাপ দিয়ে) ইনস্টল করুন।
4. ফোনে **TV Pad** খুলে ব্লুটুথ দিয়ে টিভির সাথে পেয়ার ও কানেক্ট করুন, এরপর টিভিতে **TV Arcade** খুলে ইন্টারনেট ছাড়াই গেম খেলুন!

---

## লোকালি বিল্ড করার কমান্ড

```bash
gradle :app:assembleDebug :tv:assembleDebug
```
- ফোনের APK: `app/build/outputs/apk/debug/app-debug.apk`
- টিভির APK: `tv/build/outputs/apk/debug/tv-debug.apk`
