package com.chittortech.gramseva

import com.chittortech.gramseva.data.repository.GramSevaRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MandiRepositoryTest {

    private lateinit var repository: GramSevaRepository

    @Before
    fun setUp() {
        repository = GramSevaRepository()
    }

    @Test
    fun testMandiPrices_isNotEmpty() {
        val prices = repository.getMandiPrices()
        assertTrue("Mandi prices list should not be empty", prices.isNotEmpty())
        assertTrue("Should contain at least 15 items across Rajasthan & MP", prices.size >= 15)
    }

    @Test
    fun testMandiPrices_containsKeyMewarMalwaCrops() {
        val prices = repository.getMandiPrices()
        val commodities = prices.map { it.commodity }

        assertTrue("Must contain Garlic/लहसुन", commodities.any { it.contains("लहसुन") })
        assertTrue("Must contain Wheat/गेहूं", commodities.any { it.contains("गेहूं") })
        assertTrue("Must contain Mustard/सरसों", commodities.any { it.contains("सरसों") })
        assertTrue("Must contain Onion/प्याज", commodities.any { it.contains("प्याज") })
        assertTrue("Must contain Soybean/सोयाबीन", commodities.any { it.contains("सोयाबीन") })
        assertTrue("Must contain Gram/चना", commodities.any { it.contains("चना") })
    }

    @Test
    fun testMandiPrices_containsKeyRegionalMandis() {
        val prices = repository.getMandiPrices()
        val markets = prices.map { it.market }

        assertTrue("Must cover Chittorgarh Mandi", markets.any { it.contains("चित्तौड़गढ़") })
        assertTrue("Must cover Nimbahera Mandi", markets.any { it.contains("निम्बाहेड़ा") })
        assertTrue("Must cover Neemuch Mandi (MP)", markets.any { it.contains("नीमच") })
        assertTrue("Must cover Kota Mandi", markets.any { it.contains("कोटा") })
    }

    @Test
    fun testCrossMandiComparison_Garlic() {
        val garlicComparison = repository.getMandiComparison("लहसुन (Garlic)")
        assertTrue("Should return multiple mandi rates for Garlic", garlicComparison.size >= 3)

        // Must be sorted descending by price
        val highest = garlicComparison.first()
        val lowest = garlicComparison.last()
        assertTrue("First rate should be higher than or equal to last", highest.modalPrice >= lowest.modalPrice)
        println("Top Garlic Mandi: ${highest.market} with modal rate ₹${highest.modalPrice}/Qtl")
    }

    @Test
    fun testLiveMandiFallback_executesWithoutCrash() = runBlocking {
        // Safe fallback test when API key is empty
        val rates = repository.fetchLiveMandiPrices("")
        assertNotNull(rates)
        assertTrue(rates.isNotEmpty())
    }
}

