package com.example.currency_counter.data

// Static reference content for the Guide. Bodies are plain text: paragraphs
// separated by blank lines, optional headings on lines starting with "## ".
data class GuideEntry(
    val id: String,
    val title: String,
    val category: String,
    val body: String
)

object GuideContent {
    val categories = listOf("Rare finds", "Precious metals", "History")

    val entries = listOf(
        GuideEntry(
            id = "error-notes",
            title = "Error notes worth checking for",
            category = "Rare finds",
            body = """
                Minting mistakes happen at every step of printing money, and the mistakes that slip past inspection are the ones collectors pay for. Before spending an odd-looking bill, check these common error types.

                ## Off-center prints
                A note printed visibly off its paper, so one margin is wide and the other is cut into. Small shifts are common and cheap; dramatic ones where part of the design is missing bring real premiums.

                ## Mismatched serials
                Every bill has two serial numbers, and they should match. A note whose serials differ is a genuine production error and highly collectible.

                ## Star notes
                Not technically an error: when a damaged note is caught during inspection, it is replaced with one carrying a star at the end of the serial. Most are worth face value, but rare combinations of series and Federal Reserve district can be worth far more.

                ## Inverted or misprinted seals
                A Treasury seal printed upside down, shifted, or printed over the portrait is another category collectors hunt for.

                Condition matters as much as the error itself. Crisp, uncirculated error notes command the strongest prices, while heavily worn examples are often only worth face value.
            """.trimIndent()
        ),
        GuideEntry(
            id = "reading-a-dollar",
            title = "Reading a dollar bill",
            category = "Rare finds",
            body = """
                Every US bill carries a surprising amount of information in plain sight. Knowing what each element means is the first step to spotting anything unusual.

                ## Serial number
                Two matching serials appear on each note. The leading letter matches the Federal Reserve bank that issued it, and the final character is a check letter. A star symbol replaces the final letter on replacement notes.

                ## Series year and letter
                Next to the portrait, the series year marks the design version, and a small suffix letter (A, B, C) marks minor revisions like a new Treasury Secretary signature.

                ## Federal Reserve seal and letter
                The black seal on the left identifies one of the twelve regional Federal Reserve banks, each with a number from 1 to 12.

                ## Seals and portraits
                Real notes use intaglio printing with raised texture you can feel, color-shifting ink on denominations of 10 dollars and higher, and security threads. When checking a bill, compare the fine lines: genuine engraving shows continuous, unbroken strokes rather than dot patterns.

                The more familiar you are with how a normal bill looks and feels, the more quickly an unusual one stands out.
            """.trimIndent()
        ),
        GuideEntry(
            id = "troy-ounce",
            title = "Why gold is priced per troy ounce",
            category = "Precious metals",
            body = """
                Gold, silver, platinum and palladium are all quoted in troy ounces, a unit that traces back to medieval European trade fairs and, before that, Roman coin weights.

                ## The troy ounce is not the ounce you know
                A regular (avoirdupois) ounce is 28.35 grams. A troy ounce is heavier: 31.103 grams. Precious metals keep the troy system because it descends directly from the standards merchants used when the modern bullion market formed, and changing it would break centuries of continuous pricing records.

                ## What this means in the converter
                Converting cash into troy ounces produces small numbers: one US dollar buys about 0.00026 troy ounces of gold. That is why the metals screen also shows a per-gram price, which is usually easier to reason about for small amounts.

                ## Where the prices come from
                The rates shown in this app are reference rates reported by central banks and monetary authorities, published once or a few times a day. They track the bullion market closely but are not live trading quotes, so treat them as accurate to the day, not to the minute.
            """.trimIndent()
        ),
        GuideEntry(
            id = "bullion-vs-numismatic",
            title = "Bullion value versus collector value",
            category = "Precious metals",
            body = """
                A coin can be worth two very different things at once, and knowing which one you are looking at prevents costly mistakes.

                ## Bullion value
                Bullion value is the raw metal worth: weight times the current spot price. A one-ounce silver round is worth about the silver price regardless of its age. Coins and bars sold as bullion carry small premiums over spot that reflect manufacturing and distribution.

                ## Numismatic value
                Numismatic value is collector value, driven by rarity, condition, and demand. A rare date in pristine condition can trade for many multiples of its metal content, while a common coin in worn condition is worth only its bullion.

                ## The practical test
                Ask whether the coin is interesting because of what it is made of, or because of when and how it was made. If it is the metal, price it by weight and let the converter do the arithmetic. If it is the collector story, condition grading and rarity guides matter more than any spot price.

                Reference rates are useful for the first case; they can only hint at the second.
            """.trimIndent()
        ),
        GuideEntry(
            id = "history-dollar",
            title = "A short history of the dollar",
            category = "History",
            body = """
                The US dollar descends from the Spanish silver dollar, the piece of eight that circulated worldwide in the 1700s and was legal tender in the United States until 1857.

                ## Coinage and the gold standard
                The 1792 Coinage Act established the dollar and bimetallism, fixing its value in both silver and gold. Over the 19th century gold became the anchor: paper dollars were claims on gold held by the Treasury.

                ## Greenbacks and the Federal Reserve
                The Civil War forced the first federal paper money, the greenbacks, issued without gold backing. In 1913 the Federal Reserve was created to manage the currency, and its notes gradually replaced everything else.

                ## Off gold
                In 1933 President Roosevelt ended private gold ownership domestically, and in 1971 President Nixon suspended the dollar's convertibility into gold for foreign holders, ending the gold standard for good. Since then the dollar has been fiat money: valuable because of trust in the institutions behind it rather than a promise of metal.

                The green color, the portraits, and the phrase legal tender for all debts all carry echoes of this history.
            """.trimIndent()
        ),
        GuideEntry(
            id = "history-euro",
            title = "The euro: many countries, one currency",
            category = "History",
            body = """
                The euro is the largest currency replacement in history: in 2002, twelve countries withdrew their national notes and coins overnight and switched to a single currency.

                ## From treaty to cash
                The Maastricht Treaty of 1992 set the project in motion. The euro arrived as electronic money in 1999, fixing the exchange rates of participating currencies, and physical notes and coins followed on January 1, 2002.

                ## One design, many histories
                Notes share identical designs across the euro area, using architectural styles rather than real buildings so that no country's landmarks are favored. Coins are a different story: one side is common, while the other carries a national design, so a Greek two-euro coin can turn up in Finland.

                ## Growth and strain
                The eurozone has since grown to twenty members. The shared currency weathered the debt crises of the 2010s, which underlined the core tension of the euro: one monetary policy for many different economies.

                Former currencies such as the Deutsche mark, the French franc, and the Dutch guilder are now collectors' items, with fixed conversion rates preserved forever in the historical record.
            """.trimIndent()
        )
    )

    fun byId(id: String): GuideEntry? = entries.firstOrNull { it.id == id }
}
