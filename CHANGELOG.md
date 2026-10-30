# CHANGELOG.md - Apotheosis Self Affix 変更履歴 / Update History

## [1.0.0-alpha.10] - 2026-10-31

### 日本語 (Japanese)
- **修正 & 改善 (Fixes & Improvements):**
  - **アフィックス説明文の二重表示およびDurable（耐久力）表記の完全解消:**
    - `DurableAffixMixin` を実装。Apotheosisの `DurableAffix.getDescription` および `getAugmentingText` に対して、魂アイテムの場合は本来の「耐久力XX%で消費しない」ではなく「耐久値消費の代用で+XX%経験値」を直接返すよう注入。
    - `SoulItem.appendHoverText` から自前のアフィックス走査・描画コードを完全に削除。Apotheosis本来の描画パイプライン（`ItemTooltipEvent`）に一元化。
    - これにより、Apotheosis標準のツールチップと当MODのツールチップが重なって二重表示されていた問題を根本から解消。反撃デバフ、採掘速度上昇、属性加護、耐久力の経験値代用ボーナスなど、全アフィックスが1回だけ美しく一貫して描画されるように改善。

---

### English
- **Fixes & Improvements:**
  - **Complete Resolution of Duplicate Affix Tooltips & Durable Affix Replacement:**
    - Implemented `DurableAffixMixin` to inject into `DurableAffix.getDescription` and `getAugmentingText`. For `SoulItem`, it directly returns the "+XX% XP (Substitute for Durability)" component instead of the vanilla "Chance to not consume durability".
    - Completely removed manual affix tooltip iteration from `SoulItem.appendHoverText`, consolidating all tooltip rendering into Apotheosis's native pipeline (`ItemTooltipEvent`).
    - Fixed the tooltip duplication issue where both Apotheosis and custom mod tooltips were displayed simultaneously. All affixes (counter-attack debuffs, haste on mine, attributes, and converted durability XP bonuses) now render cleanly and unified without duplicates.

---

## [1.0.0-alpha.9] - 2026-10-31

### 日本語 (Japanese)
- **修正 & 改善 (Fixes & Improvements):**
  - **重複する頭/胴体/レギンス/靴スロット属性ツールチップの完全除去:**
    - `ApotheosisSelfAffix` における `SOUL_CATEGORY` の登録スロットを空配列（`new EquipmentSlot[0]`）に戻し、バニラの全装備スロット（頭/胴体/脚/足）が登録されていた名残を解消。
    - `SoulItem.getAttributeModifiers(EquipmentSlot, ItemStack)` を明示的にオーバーライドし、バニラスロット用モディファイアとして常に空を返すよう修正。
    - `SoulAffixEventHandler` に `ItemAttributeModifierEvent` ハンドラを追加し、Soul アイテムに対するバニラスロット用モディファイアの割り込み注入を完全に破棄（`event.clearModifiers()`）。
    - これにより、ツールチップが縦に極端に膨張して最上部の物理ダメージ軽減やポーション付与などのアフィックスが見切れる問題を完全に解消し、Curios 魂スロット用の属性のみをスッキリ表示するように改善。

---

### English
- **Fixes & Improvements:**
  - **Complete Removal of Redundant Head/Chest/Legs/Feet Slot Attribute Tooltips:**
    - Reverted `SOUL_CATEGORY` registration slots to an empty array (`new EquipmentSlot[0]`), cleaning up the legacy behavior that registered all vanilla equipment slots.
    - Explicitly overrode `SoulItem.getAttributeModifiers(EquipmentSlot, ItemStack)` to always return an empty multimap for vanilla equipment slots.
    - Added an `ItemAttributeModifierEvent` subscriber in `SoulAffixEventHandler` that completely clears any vanilla slot modifiers injected into `SoulItem` via `event.clearModifiers()`.
    - Resolved the critical screen-overflow issue where tooltips were bloated by four vanilla slot sections, hiding top-level defensive ability affixes (damage reductions, potion effects). Only the clean Curios Soul slot attributes and ability descriptions are now displayed.

---

## [1.0.0-alpha.8] - 2026-10-31

### 日本語 (Japanese)
- **修正 & 改善 (Fixes & Improvements):**
  - **インベントリからCuriosスロットへの意図しない複製バグの完全修正:**
    - `PlayerTickEvent` によるバックグラウンド自動移動・定期監視処理を完全に撤廃。インベントリ操作中やリフォージ台での作業中に勝手に裏でアイテムが移動・複製される挙動を解消。
    - ログイン時およびリスポーン時において、プレイヤーが手持ちインベントリに魂を所持している場合はCuriosスロットへの強制装備や複製を行わず、プレイヤーの所持状態を尊重する仕様に整理。
    - プレイヤーがスロットにもインベントリにも魂を所持していない場合（アイテム化して落下中、溶岩・奈落等でロスト時）のみ、救済措置として新規魂をスロットへ補充。
  - **ツールチップ描画の多層フェイルセーフ化（防具アフィックス・属性の確実表示）:**
    - `SoulAffixHelper.getAffixesSafe` を導入。Apotheosisのキャッシュや判定の有無に依存せず、アイテムのNBTから直接アフィックスおよび数値を100%確実に復元。
    - `SoulItem.appendHoverText` で、防具アフィックス能力（物理ダメージ軽減、落下ダメージ軽減等の説明文）および属性加護（Attribute値）を直接ツールチップに描画する処理を強化。

---

### English
- **Fixes & Improvements:**
  - **Fixed Unintended Soul Duplication from Inventory to Curios:**
    - Completely removed background tick monitoring (`PlayerTickEvent`) that caused synchronization desyncs and duplicated souls during inventory/reforging operations.
    - Synchronized login/respawn logic to respect player inventory state: if the player already holds a soul in their regular inventory, it will no longer duplicate or force-move it.
    - Relief soul is now strictly supplied only when completely devoid of any soul (lost to void/lava or dropped in world).
  - **Multi-layered Fail-safe Tooltip Rendering (Armor Affixes & Attributes):**
    - Introduced `SoulAffixHelper.getAffixesSafe` to ensure 100% reliable affix reconstruction directly from stack NBT data.
    - Enhanced `SoulItem.appendHoverText` to directly render all defensive abilities (e.g. physical damage reduction, feather falling) and attribute modifiers onto the tooltip.

---

## [1.0.0-alpha.7] - 2026-10-31

### 日本語 (Japanese)
- **修正 & 改善 (Fixes & Improvements):**
  - **ツールチップで防具アフィックス・特殊能力が表示されない根本原因の完全解消:**
    - `AffixHelperMixin` を実装。Apotheosisの `AffixHelper.getAffixesImpl` が内部のカテゴリ検証（`canApplyTo`）によって `SoulItem` のアフィックスを空として破棄していた重大な問題を解消。NBTに保存された全アフィックスが正常に復元され、Apotheosis標準のツールチップ描画、防具アフィックス能力、属性加護が100%確実にツールチップに描画されるよう修正。
  - **魂の救済生成仕様の復元と上書き防止の調和:**
    - ユーザー指定の「ロスト時や回収困難時の救済措置」を維持。プレイヤーが魂スロットにもインベントリにも魂を持っていない状態の場合、自動的に新規魂を魂スロットに補充。
    - 既存の装備中アフィックス済み魂に対する上書きは厳格に防止（スロットが占有されている場合は一切の操作を行わない安全設計）。

---

### English
- **Fixes & Improvements:**
  - **Complete Resolution of Missing Armor Affixes / Abilities on Tooltips:**
    - Implemented `AffixHelperMixin` into Apotheosis's `AffixHelper.getAffixesImpl` to resolve the root cause where `SoulItem` affixes were being discarded during category verification (`canApplyTo`). All affixes are now cleanly restored from NBT, allowing Apotheosis's native tooltip rendering, defensive effects, and attributes to display properly.
  - **Restored Lost/Drop Relief Mechanism While Safeguarding Affixed Souls:**
    - Preserved the recovery mechanism per user specification: if a player has lost their soul or dropped it out of reach (no soul in slot or inventory), a new soul is automatically supplied to the empty slot.
    - Strictly ensured existing equipped souls are never overwritten.

---

## [1.0.0-alpha.6] - 2026-10-31

### 日本語 (Japanese)
- **修正 & 改善 (Fixes & Improvements):**
  - **魂の増殖防止 & 上書き防止の完全改修:**
    - ログイン時およびリスポーン時、プレイヤーがすでに魂を所持しているか（Curios全スロット、メインインベントリ、防具、オフハンド、マウスカーソル保持アイテム）を包括的に検証する判定（`hasAnySoulInPossession`）を実装。
    - プレイヤーの永続タグ（`PlayerPersisted`）配下に配布済みフラグ（`GIVEN_TAG`）を保存し、一度でも配布されたプレイヤーには二度と新規生成を行わないよう厳格化。
    - スロットにアイテムが存在する場合は上書き（`setEquippedCurio`）を絶対に実行しない安全ガードを追加。
    - 魂スロットが空で、かつ手持ちインベントリに既存の魂（アフィックス済み含む）が存在する場合は、新規生成せず既存の魂を魂スロットへ安全に移動・装備するスマート装着処理を導入。

---

### English
- **Fixes & Improvements:**
  - **Complete Overhaul of Duplicate Prevention & Overwrite Protection:**
    - Implemented comprehensive possession checks (`hasAnySoulInPossession`) inspecting all Curios slots, player inventory, armor, offhand, and cursor-carried stacks before attempting any soul operations.
    - Persisted the given flag (`GIVEN_TAG`) under `PlayerPersisted` to permanently prevent redundant soul generation on relogin or respawn.
    - Added strict overwrite prevention ensuring occupied soul slots are never overwritten by new items.
    - Implemented a smart equip feature that transfers an existing affixed Soul from the main inventory into an empty soul slot instead of fabricating duplicates.

---

## [1.0.0-alpha.5] - 2026-10-31

### 日本語 (Japanese)
- **修正 & 改善 (Fixes & Improvements):**
  - **ソケットのみ付与・アフィックス非表示の根本解決:**
    - `SOUL_CATEGORY` に全装備スロット（`EquipmentSlot.values()`）を割り当て、Apotheosis内部のスロット適合判定で属性アフィックス（`AttributeAffix`）が確実に適用されるよう修正。
    - `SoulItem` のツールチップ描画を大幅強化。魂に付与されたアフィックス効果（Ability）および属性加護（攻撃力、移動速度、防御力などの各種Attribute）の数値を美しく一覧表示するように改善。
  - **エソテリック（Esoteric）等カスタムレアリティの完全対応:**
    - Apotheotic Additions や Esoteric Reforging が追加する独自高位レアリティ（`esoteric`, `artifact`, `heirloom` 等）において、バニラApotheosisのアフィックスが安全に適用されるようフォールバックレアリティ機構（`getEffectiveRarity`）を実装。
    - 上位レアリティにおけるステータス自動スケーリング処理を導入（下位アフィックス値から階層差に応じたボーナス加算）。
  - **アフィックス効果発動の完全網羅 & バグ修正:**
    - 攻撃時ダメージ増加処理で攻撃者自身の魂（`attackerSoul`）を参照するように修正。
    - 盾防御アフィックス（`safeOnShieldBlock`）、弓射撃アフィックス（`safeOnArrowFired`）、矢命中アフィックス（`safeOnArrowImpact`）のハンドラを追加し、武器・防具の全アフィックスがプレイヤーに宿るように拡充。

---

### English
- **Fixes & Improvements:**
  - **Resolved Missing Affixes / Only Sockets Applied Issue:**
    - Assigned all equipment slots (`EquipmentSlot.values()`) to `SOUL_CATEGORY`, ensuring Apotheosis's internal slot matching successfully applies all `AttributeAffix` modifiers.
    - Heavily enhanced `SoulItem` hover tooltip rendering to clearly list both ability affix descriptions and attribute modifiers (attack damage, speed, armor, etc.) with custom color coding.
  - **Full Support for Custom Rarities (Esoteric, Artifact, Heirloom):**
    - Implemented a smart fallback rarity resolver (`getEffectiveRarity`) allowing standard Apotheosis affixes to be rolled and applied seamlessly under custom tiers introduced by Apotheotic Additions and Esoteric Reforging.
    - Added dynamic attribute scaling for higher tiers exceeding base affix definitions.
  - **Complete Affix Trigger Coverage & Bug Fixes:**
    - Fixed attacker soul reference bug in offensive damage bonus calculation.
    - Added event hooks for shield blocking (`safeOnShieldBlock`), arrow shooting (`safeOnArrowFired`), and projectile impacts (`safeOnArrowImpact`), fully bridging all weapon and armor behaviors to the equipped soul.

---

## [1.0.0-alpha.4] - 2026-10-31

### 日本語 (Japanese)
- **修正 (Fixes):**
  - リフォージ台（Esoteric Reforging Table含む）で魂アイテムをリフォージした際、Apotheosisのシャッフル処理（`Collections.shuffle`）で `UnsupportedOperationException` が発生しクラッシュする不具合を修正（Mixinが返却する利用可能アフィックスリストを不変リストから可変の `ArrayList` に変更）。

---

### English
- **Fixes:**
  - Fixed a crash (`UnsupportedOperationException`) when reforging the Soul in reforging tables (including Esoteric Reforging Table) during Apotheosis's affix shuffling (`Collections.shuffle`) by wrapping the available affixes list in a mutable `ArrayList`.

---

## [1.0.0-alpha.3] - 2026-10-31

### 日本語 (Japanese)
- **修正 (Fixes):**
  - Curios API において魂スロットがプレイヤーに付与されない問題を修正：
    - `curios/entities/entities.json` を追加し、`minecraft:player` に対する `soul` スロットの割り当てを明示。
    - `InterModComms (IMC)` による `SlotTypeMessage.REGISTER_TYPE` の送信処理を追加し、データパックとコード双方から確実にスロットが登録される二重登録構成を採用。
  - プレイヤーログイン時および初期化遅延時の魂自動装備処理（`equipSoulIfMissing`）を強化し、セーフティネットタイマーを追加。

---

### English
- **Fixes:**
  - Fixed an issue where the Soul slot was not attached to players in Curios:
    - Added `curios/entities/entities.json` to explicitly assign the `soul` slot to `minecraft:player`.
    - Added InterModComms (IMC) registration via `SlotTypeMessage.REGISTER_TYPE` to guarantee slot creation from both datapack and runtime channels.
  - Enhanced initial Soul auto-equipping logic with a periodic safety check to handle delayed Curios inventory initialization.

---

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
