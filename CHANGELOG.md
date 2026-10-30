# CHANGELOG.md - Apotheosis Self Affix 変更履歴 / Update History

## [1.0.0-alpha.2] - 2026-10-31

### 日本語 (Japanese)
- **修正 (Fixes):**
  - `META-INF/mods.toml` における Apothic Attributes の依存先 modId を正しい `attributeslib` に修正（`apothic_attributes` による起動時依存解決エラー・クラッシュを解消）。
  - `apothic-infinite` の公式 modId（`apothic_infnite`）に合わせて依存関係定義を同期。

---

### English
- **Fixes:**
  - Corrected the Apothic Attributes dependency modId in `META-INF/mods.toml` to `attributeslib` (resolved crash caused by unresolved `apothic_attributes` dependency).
  - Synchronized `apothic-infinite` optional dependency modId with its official ID (`apothic_infnite`).

---

## [1.0.0-alpha.1] - 2026-10-31

### 日本語 (Japanese)
- **新規追加 (Features):**
  - Curios API 専用の「魂スロット（Soul Slot）」を1枠追加。
  - 専用アイテム「魂（Soul）」の実装。ユーザー提供の16x16 Minecraft Dungeons風テクスチャを採用。
  - 初回ログイン・スポーン時の魂自動装備機能（設定可能）。
  - プレイヤー死亡時の魂アイテム完全引き継ぎ機能（NBT、レアリティ、アフィックスを保持）。
  - Apotheosis リフォージ台（Reforging Table）との連携機能：
    - 魂アイテムが武器および防具のアフィックスを両方受け入れ可能（複合アフィックス対象）。
    - 「ダメージを受けたとき特定ポーション効果発動」「ダメージ軽減」などの防具アフィックス。
    - 「ダメージを与えたときポーション効果付与」「落雷」「なぎ払い」「処刑」などの武器アフィックス。
    - 各種属性値（攻撃力、防具、クリティカル率、ライフスティールなど）をCurios経由で直接プレイヤーに適用。
    - 耐久力消費免除（Durable）アフィックスの代用として「経験値獲得量増加」効果を付与。
  - 外部MOD対応：
    - `apotheotic-additions` 対応
    - `apothic-infinite` 対応
    - `esoteric-reforging` 対応
  - 日英バイリンガル言語ファイル（`en_us.json`, `ja_jp.json`）および各種ドキュメント（`README.md`, `CHANGELOG.md`, `CONFIG.md`）の同梱。

---

### English
- **New Features:**
  - Added dedicated 1-slot "Soul Slot" via Curios API.
  - Implemented the "Soul" item with user-crafted 16x16 Minecraft Dungeons inspired texture.
  - Automatic initial equipping of the Soul on player login/spawn (configurable).
  - Preservation of the Soul across player death (retaining all NBT data, rarity, and affixes).
  - Full Apotheosis Reforging Table compatibility:
    - Soul can roll and receive both weapon and armor affixes simultaneously.
    - Defensive affixes: on-hurt potion effects, damage reduction protection, counter-effects.
    - Offensive affixes: on-hit potion effects, thunderstruck, cleaving, executing, damage bonuses.
    - Full Attribute modifier integration: attack damage, armor, crit chance, life steal, etc., applied directly to the player through Curios.
    - Durable affix substitution: converts durability loss reduction chance into bonus experience gain.
  - Cross-mod compatibility:
    - Compatible with `apotheotic-additions`.
    - Compatible with `apothic-infinite`.
    - Compatible with `esoteric-reforging`.
  - Bilingual documentation and localization in both English (`en_us.json`) and Japanese (`ja_jp.json`).
