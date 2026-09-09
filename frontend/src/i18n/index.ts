export type Locale = "zh" | "en";

const dict = {
  zh: {
    title: "心情地图",
    subtitle: "附近的此刻 · 约 24 小时后淡去",
    locate: "定位",
    locating: "定位中…",
    tapMap: "允许定位，或点地图选位置",
    locationDenied: "定位未开启，点地图放置心情",
    notePlaceholder: "一句短注（可选）",
    dropHint: "点一下心情即可留下",
    dropped: "已留在地图上",
    nickname: "昵称（可选）",
    nicknamePlaceholder: "怎么称呼你",
    save: "保存",
    cancel: "取消",
    anon: "匿名",
    you: "我",
    nearbyEmpty: "附近还很安静。成为第一个留下心情的人。",
    delete: "撤下",
    error: "出了点问题，请再试一次",
    wait: "请稍后再留一次",
    lang: "EN",
    HAPPY: "开心",
    CALM: "平静",
    SAD: "难过",
    ANGRY: "生气",
    ANXIOUS: "焦虑",
    EXCITED: "兴奋",
    TIRED: "疲惫",
    GRATEFUL: "感恩",
    LOVED: "被爱",
    OKAY: "还好",
  },
  en: {
    title: "Mood Map",
    subtitle: "Nearby, right now · fades in ~24h",
    locate: "Locate me",
    locating: "Finding you…",
    tapMap: "Allow location, or tap the map",
    locationDenied: "Location off — tap the map to place a mood",
    notePlaceholder: "A short note (optional)",
    dropHint: "One tap to leave a mood",
    dropped: "Pinned on the map",
    nickname: "Nickname (optional)",
    nicknamePlaceholder: "What should we call you",
    save: "Save",
    cancel: "Cancel",
    anon: "anon",
    you: "you",
    nearbyEmpty: "Quiet around here. Be the first to drop a mood.",
    delete: "Remove",
    error: "Something went wrong. Try again.",
    wait: "Wait a moment before another pin",
    lang: "中文",
    HAPPY: "happy",
    CALM: "calm",
    SAD: "sad",
    ANGRY: "angry",
    ANXIOUS: "anxious",
    EXCITED: "excited",
    TIRED: "tired",
    GRATEFUL: "grateful",
    LOVED: "loved",
    OKAY: "okay",
  },
} as const;

export type MessageKey = keyof typeof dict.zh;

export function detectLocale(): Locale {
  const stored = localStorage.getItem("moodmap.locale");
  if (stored === "zh" || stored === "en") {
    return stored;
  }
  return navigator.language.toLowerCase().startsWith("zh") ? "zh" : "en";
}

export function t(locale: Locale, key: MessageKey): string {
  return dict[locale][key];
}
