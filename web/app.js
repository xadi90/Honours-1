const DEPARTMENTS = [
  "রাষ্ট্রবিজ্ঞান (Political Science)",
  "বাংলা", "ইংরেজি", "ইতিহাস", "ইসলামের ইতিহাস ও সংস্কৃতি",
  "দর্শন", "সমাজবিজ্ঞান", "সমাজকর্ম", "অর্থনীতি", "মনোবিজ্ঞান",
  "ভূগোল ও পরিবেশ", "গণিত", "পদার্থবিজ্ঞান", "রসায়ন",
  "উদ্ভিদবিজ্ঞান", "প্রাণিবিজ্ঞান", "হিসাববিজ্ঞান", "ব্যবস্থাপনা",
  "মার্কেটিং", "ফিন্যান্স ও ব্যাংকিং", "পরিসংখ্যান", "অন্যান্য অনার্স বিভাগ"
];

document.getElementById("year").textContent = new Date().getFullYear();

const deptContainer = document.getElementById("dept-list");
DEPARTMENTS.forEach((name) => {
  const span = document.createElement("span");
  span.className = "pill";
  span.textContent = name;
  deptContainer.appendChild(span);
});

fetch("https://api.github.com/repos/xadi90/Honours-1/releases/latest")
  .then((res) => (res.ok ? res.json() : null))
  .then((data) => {
    const el = document.getElementById("release-status");
    const btn = document.getElementById("apk-download-btn");
    if (data && data.tag_name) {
      el.textContent = `Latest Release: ${data.tag_name} • Free Android APK`;
      const apkAsset = Array.isArray(data.assets)
        ? data.assets.find((a) => a.browser_download_url && a.browser_download_url.endsWith(".apk"))
        : null;
      if (btn && apkAsset) {
        btn.href = apkAsset.browser_download_url;
      }
    } else {
      el.textContent = "Honours Solution v1.0 • National University Bangladesh";
    }
  })
  .catch(() => {
    document.getElementById("release-status").textContent = "Honours Solution v1.0 • National University Bangladesh";
  });
