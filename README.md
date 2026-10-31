# Apotheosis Self Affix

[![Minecraft 1.20.1](https://img.shields.io/badge/Minecraft-1.20.1-blue.svg)](https://minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-47.4.0-orange.svg)](https://files.minecraftforge.net/)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

---

## 概要 / Overview

### 日本語 (Japanese)
**Apotheosis Self Affix** は、**Apotheosis** と連携し、プレイヤー自身の「魂（Soul）」にアフィックスを施して自己を強化する拡張MODです。

プレイヤー固有の専用「魂スロット」が追加され、プレイヤーは最初から自身の魂を身に宿しています（右クリック即時装備や `O` キーの専用GUI、インベントリボタンからいつでもアクセス可能）。リフォージ台で魂にアフィックスを付与することで、武器と防具の双方のアフィックス効果をプレイヤー自身に恒久的に宿らせることができます。

#### 主な特徴
- **プレイヤー固有の独立「魂スロット」:** Curiosに依存しない完全独自Capabilityによるスロット管理（Curiosとの競合ゼロ）。
- **専用GUI「魂の祭壇」:** `O` キー、サバイバルインベントリのアイコンボタン、または `/soul` コマンドで開閉可能。バニラライクなデザイン。
- **デフォルト初期装備 & 死亡時引き継ぎ:** 初回スポーン時に自動で魂が装備され、死んでもアフィックスやNBTデータを含めて安全に引き継がれます。
- **武器＋防具の複合アフィックス付与:**
  - **被弾時効果:** ダメージ軽減、被弾時ポーション発動（再生、耐性など）、反撃効果（盲目など）。
  - **攻撃時効果:** 攻撃時ポーション付与（毒、ウィザーなど）、落雷、なぎ払い、処刑効果など。
  - **属性値（Attributes）直接強化:** 攻撃力、防具、クリティカル率、ライフスティール、最大体力等がプレイヤーへ直接加算。
  - **耐久免除の経験値代用:** 魂には耐久力がないため、Durable（耐久免除）アフィックスが付与された場合は **経験値獲得量増加ボーナス** に自動変換！
- **外部MODとの高度な互換性:**
  - `apotheotic-additions` 対応
  - `apothic-infinite` 対応（複数Tierの混在アフィックスに完全対応）
  - `esoteric-reforging` 対応（Esoteric / Ancient などの高階級アフィックスに完全対応）
- **Minecraft Dungeons スタイルの専用テクスチャ (16x16)**

#### 導入要件
- Minecraft: 1.20.1
- Minecraft Forge: 47.4.0 以上
- Apotheosis (および Apothic Attributes, Placebo)
- Curios API (任意)

---

### English
**Apotheosis Self Affix** is an expansion mod for **Apotheosis**, allowing players to reforge and empower their very own "Soul" with powerful affixes.

A dedicated "Soul Slot" is integrated into the player's soul capability, pre-equipped with your Soul from the start. Accessible via keybind (default `O`), inventory button, right-click equip, or `/soul` command. By forging affixes onto your Soul at a Reforging Table, both weapon and armor affix capabilities become channeled directly into the player.

#### Key Features
- **Independent "Soul Slot":** Dedicated player capability with zero dependency on Curios, immune to external slot tag conflicts.
- **Dedicated "Soul Altar" GUI:** Accessible via `O` key, survival inventory button, or `/soul` command. Beautiful vanilla-styled container.
- **Pre-equipped & Preserved Across Death:** Automatically equipped upon first entering a world and preserved (including all NBT and affixes) through player death.
- **Combined Weapon & Armor Affixes:**
  - **Defensive Affixes:** Damage reduction, on-hurt potion triggers (regeneration, resistance), and counter-effects.
  - **Offensive Affixes:** On-hit potion ailments (poison, wither), thunderstruck, cleaving, and execution triggers.
  - **Direct Attribute Scaling:** Attack damage, armor, critical chance, life steal, and max health feed straight into player attributes.
  - **Durability Affix Substitution:** Since Souls are indestructible, the Durable affix converts into a **Bonus Experience Gain** booster!
- **Broad Cross-Mod Compatibility:**
  - Compatible with `apotheotic-additions`
  - Compatible with `apothic-infinite` (multi-tier affix support)
  - Compatible with `esoteric-reforging` (Esoteric / Ancient tier support)
- **Authentic 16x16 Minecraft Dungeons inspired Soul texture.**

#### Requirements
- Minecraft: 1.20.1
- Minecraft Forge: 47.4.0+
- Apotheosis (plus Apothic Attributes and Placebo)
- Curios API (Optional)

---

## ライセンス / License
This project is licensed under the **GNU General Public License v3.0 (GPL-3.0-or-later)**.
