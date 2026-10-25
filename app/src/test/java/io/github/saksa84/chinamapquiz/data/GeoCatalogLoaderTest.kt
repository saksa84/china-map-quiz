package io.github.saksa84.chinamapquiz.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoCatalogLoaderTest {

    // --- Outils ---

    private fun readResource(path: String): String =
        requireNotNull(javaClass.classLoader?.getResource(path)) { "Ressource introuvable : $path" }.readText()

    private val fixtures = listOf("provinces", "regions", "features").map { readResource("geo/fixture_$it.json") }

    private fun loadFixtures(): GeoCatalog =
        when (val result = GeoCatalogLoader.load(*fixtures.toTypedArray())) {
            is LoadResult.Success -> result.catalog
            else -> throw AssertionError("Les fichiers de test devraient être valides : $result")
        }

    /** Une entité en JSON ; seuls les champs utiles au test sont renseignés. */
    private fun entity(
        id: String,
        type: String = "province",
        name: String = "Sichuan",
        parentId: String? = "CN",
        memberIds: List<String> = emptyList(),
        sourceId: String = "s",
    ): String {
        val parent = parentId?.let { ""","parentId":"$it"""" } ?: ""
        val members = if (memberIds.isEmpty()) "" else ""","memberIds":[${memberIds.joinToString(",") { "\"$it\"" }}]"""
        return """{"id":"$id","type":"$type","name":"$name","sourceId":"$sourceId"$parent$members}"""
    }

    /** Un fichier contenant le pays "CN" suivi des entités données. */
    private fun fileWith(vararg entities: String, schemaVersion: Int = 1): String {
        val all = listOf(entity("CN", type = "country", name = "Zhongguo", parentId = null)) + entities
        return """{"schemaVersion":$schemaVersion,
            "sources":[{"id":"s","name":"test","license":"test"}],
            "entities":[${all.joinToString(",")}]}"""
    }

    private fun errorsOf(vararg texts: String): List<ValidationError> =
        when (val result = GeoCatalogLoader.load(*texts)) {
            is LoadResult.Invalid -> result.errors
            else -> throw AssertionError("Erreurs de validation attendues, obtenu : $result")
        }

    private fun assertValid(vararg texts: String) {
        val result = GeoCatalogLoader.load(*texts)
        assertTrue("Attendu valide, obtenu : $result", result is LoadResult.Success)
    }

    // --- Fichiers de test valides ---

    @Test
    fun fixtures_loadAllCategories() {
        val catalog = loadFixtures()
        assertEquals(4, catalog.inCategory(Category.PROVINCIAL).size)
        assertEquals(4, catalog.inCategory(Category.REGION).size)
        assertEquals(1, catalog.inCategory(Category.CITY).size)
        assertEquals(2, catalog.inCategory(Category.RIVER).size)
        assertEquals(3, catalog.inCategory(Category.MOUNTAIN).size)
    }

    @Test
    fun fixtures_provincesAreChildrenOfChina() {
        val catalog = loadFixtures()
        val provincialChildren = catalog.children("CN").filter { it.category == Category.PROVINCIAL }
        assertEquals(setOf("CN-BJ", "CN-SC", "CN-XZ", "CN-HB"), provincialChildren.map { it.id }.toSet())
        assertNull(catalog["CN"]?.parentId)
    }

    @Test
    fun fixtures_keepTypesAliasesAndMetadata() {
        val catalog = loadFixtures()
        val xizang = catalog["CN-XZ"]!!
        assertEquals(FeatureType.AUTONOMOUS_REGION, xizang.type)
        assertEquals(listOf("Tibet"), xizang.aliases)
        assertEquals("西藏", xizang.hanzi)
        assertEquals("Lhasa", xizang.capital)
        assertEquals(FeatureType.MUNICIPALITY, catalog["CN-BJ"]!!.type)
    }

    @Test
    fun fixtures_geometryKindDependsOnType() {
        val catalog = loadFixtures()
        assertEquals(GeometryKind.POLYGON, catalog["CN-SC"]!!.type.geometry)
        assertEquals(GeometryKind.COMPOSITE, catalog["RG-XINAN"]!!.type.geometry)
        assertEquals(GeometryKind.POINT, catalog["CT-CHENGDU"]!!.type.geometry)
        assertEquals(GeometryKind.LINE, catalog["RV-CHANGJIANG"]!!.type.geometry)
        assertEquals(GeometryKind.POLYGON, catalog["MT-HIMALAYA"]!!.type.geometry)
        assertEquals(GeometryKind.POINT, catalog["PK-QOMOLANGMA"]!!.type.geometry)
    }

    @Test
    fun fixtures_regionsResolveTheirProvinces() {
        val catalog = loadFixtures()
        assertEquals(listOf("CN-SC", "CN-XZ"), catalog.membersOf("RG-XINAN").map { it.id })
        assertEquals("RG-XINAN", catalog.topRegionOf("CN-SC")?.id)
        // Hubei est dans Huazhong, mais sa grande région de premier rang est Zhongnan.
        assertEquals("RG-ZHONGNAN", catalog.topRegionOf("CN-HB")?.id)
        assertEquals(listOf("RG-HUAZHONG"), catalog.children("RG-ZHONGNAN").map { it.id })
    }

    @Test
    fun fixtures_riversAndMountainsHaveTheirOwnHierarchy() {
        val catalog = loadFixtures()
        assertEquals(listOf("RV-JIALINGJIANG"), catalog.children("RV-CHANGJIANG").map { it.id })
        assertEquals(listOf("RV-CHANGJIANG"), catalog.ofType(FeatureType.MAJOR_RIVER).map { it.id })
        assertEquals(listOf("PK-QOMOLANGMA"), catalog.children("MT-HIMALAYA").map { it.id })
        assertNull("Un sommet peut être isolé", catalog["PK-TAISHAN"]!!.parentId)
        assertEquals("CN-SC", catalog["CT-CHENGDU"]!!.parentId)
    }

    @Test
    fun lookup_unknownIdReturnsEmptyResults() {
        val catalog = loadFixtures()
        assertNull(catalog["CN-XX"])
        assertTrue(catalog.children("CN-XZ").isEmpty())
        assertTrue(catalog.membersOf("RG-XX").isEmpty())
        assertNull(catalog.topRegionOf("CN-XX"))
    }

    // --- Plusieurs fichiers ---

    @Test
    fun citiesFileAlone_failsBecauseItsProvinceIsMissing() {
        val errors = errorsOf(readResource("geo/fixture_features.json"))
        assertTrue(ValidationError.UnknownParent("CT-CHENGDU", "CN-SC") in errors)
    }

    @Test
    fun sameIdInTwoFiles_isRejected() {
        val errors = errorsOf(fileWith(entity("CN-SC")), fileWith())
        assertEquals(listOf(ValidationError.DuplicateId("CN")), errors)
    }

    @Test
    fun conflictingSourcesAcrossFiles_areRejected() {
        val other = """{"schemaVersion":1,"sources":[{"id":"s","name":"autre","license":"test"}],"entities":[]}"""
        assertEquals(listOf(ValidationError.ConflictingSource("s")), errorsOf(fileWith(), other))
    }

    // --- JSON illisible ---

    @Test
    fun malformedJson_isReported() {
        assertTrue(GeoCatalogLoader.load("{ pas du json") is LoadResult.Malformed)
    }

    @Test
    fun unknownFeatureType_isReported() {
        assertTrue(GeoCatalogLoader.load(fileWith(entity("CN-SC", type = "canton"))) is LoadResult.Malformed)
    }

    @Test
    fun missingRequiredField_isReported() {
        val text = fileWith("""{"id":"CN-SC","type":"province","parentId":"CN","sourceId":"s"}""")
        assertTrue(GeoCatalogLoader.load(text) is LoadResult.Malformed)
    }

    // --- Identifiants, sources, schéma ---

    @Test
    fun validMinimalFile_hasNoErrors() {
        assertValid(fileWith(entity("CN-SC")))
    }

    @Test
    fun duplicateId_isRejected() {
        val errors = errorsOf(fileWith(entity("CN-SC"), entity("CN-SC", name = "Sichuan bis")))
        assertEquals(listOf(ValidationError.DuplicateId("CN-SC")), errors)
    }

    @Test
    fun unknownSource_isRejected() {
        val errors = errorsOf(fileWith(entity("CN-SC", sourceId = "inconnue")))
        assertEquals(listOf(ValidationError.UnknownSource("CN-SC", "inconnue")), errors)
    }

    @Test
    fun unsupportedSchemaVersion_isRejected() {
        assertEquals(listOf(ValidationError.UnsupportedSchema(2)), errorsOf(fileWith(schemaVersion = 2)))
    }

    // --- Parents ---

    @Test
    fun unknownParent_isRejected() {
        assertEquals(listOf(ValidationError.UnknownParent("CN-SC", "XX")), errorsOf(fileWith(entity("CN-SC", parentId = "XX"))))
    }

    @Test
    fun missingRequiredParent_isRejected() {
        assertEquals(listOf(ValidationError.MissingParent("CN-SC")), errorsOf(fileWith(entity("CN-SC", parentId = null))))
        val tributary = entity("RV-A", type = "tributary", name = "Min Jiang", parentId = null)
        assertEquals(listOf(ValidationError.MissingParent("RV-A")), errorsOf(fileWith(tributary)))
    }

    @Test
    fun provinceUnderProvince_isRejected() {
        val errors = errorsOf(fileWith(entity("CN-SC"), entity("CN-YN", name = "Yunnan", parentId = "CN-SC")))
        assertEquals(listOf(ValidationError.InvalidParent("CN-YN", "CN-SC")), errors)
    }

    @Test
    fun countryWithParent_isRejected() {
        val errors = errorsOf(fileWith(entity("CN2", type = "country", name = "Zhongguo", parentId = "CN")))
        assertEquals(listOf(ValidationError.InvalidParent("CN2", "CN")), errors)
    }

    @Test
    fun tributaryOfACity_isRejected() {
        val errors = errorsOf(
            fileWith(
                entity("CN-SC"),
                entity("CT-A", type = "city", name = "Chengdu", parentId = "CN-SC"),
                entity("RV-A", type = "tributary", name = "Min Jiang", parentId = "CT-A"),
            ),
        )
        assertEquals(listOf(ValidationError.InvalidParent("RV-A", "CT-A")), errors)
    }

    @Test
    fun cityDirectlyUnderCountry_isRejected() {
        val errors = errorsOf(fileWith(entity("CT-A", type = "city", name = "Chengdu", parentId = "CN")))
        assertEquals(listOf(ValidationError.InvalidParent("CT-A", "CN")), errors)
    }

    @Test
    fun peakWithoutRange_isAccepted() {
        assertValid(fileWith(entity("PK-A", type = "peak", name = "Tai Shan", parentId = null)))
    }

    @Test
    fun selfParent_isRejected() {
        val errors = errorsOf(fileWith(entity("RV-A", type = "tributary", name = "Min Jiang", parentId = "RV-A")))
        assertEquals(listOf(ValidationError.InvalidParent("RV-A", "RV-A")), errors)
    }

    // --- Grandes régions ---

    @Test
    fun regionWithoutMembers_isRejected() {
        val errors = errorsOf(fileWith(entity("CN-SC"), entity("RG-A", type = "region", name = "Xinan")))
        assertTrue(ValidationError.EmptyRegion("RG-A") in errors)
    }

    @Test
    fun regionWithUnknownOrNonProvincialMember_isRejected() {
        val errors = errorsOf(
            fileWith(
                entity("CN-SC"),
                entity("CT-A", type = "city", name = "Chengdu", parentId = "CN-SC"),
                entity("RG-A", type = "region", name = "Xinan", memberIds = listOf("CN-SC", "CN-XX", "CT-A")),
            ),
        )
        assertEquals(
            listOf(ValidationError.UnknownMember("RG-A", "CN-XX"), ValidationError.InvalidMember("RG-A", "CT-A")),
            errors,
        )
    }

    @Test
    fun membersOnNonRegion_areRejected() {
        val errors = errorsOf(fileWith(entity("CN-SC"), entity("CN-YN", name = "Yunnan", memberIds = listOf("CN-SC"))))
        assertEquals(listOf(ValidationError.UnexpectedMembers("CN-YN")), errors)
    }

    @Test
    fun subRegionMemberOutsideParentRegion_isRejected() {
        val errors = errorsOf(
            fileWith(
                entity("CN-HB", name = "Hubei"),
                entity("CN-GD", name = "Guangdong"),
                entity("RG-ZN", type = "region", name = "Zhongnan", memberIds = listOf("CN-HB")),
                entity("RG-HZ", type = "region", name = "Huazhong", parentId = "RG-ZN", memberIds = listOf("CN-HB", "CN-GD")),
            ),
        )
        assertTrue(ValidationError.MemberOutsideParentRegion("RG-HZ", "CN-GD") in errors)
    }

    @Test
    fun provinceInNoRegionOrInTwoRegions_isRejected() {
        val errors = errorsOf(
            fileWith(
                entity("CN-SC"),
                entity("CN-YN", name = "Yunnan"),
                entity("CN-HB", name = "Hubei"),
                entity("RG-A", type = "region", name = "Xinan", memberIds = listOf("CN-SC", "CN-YN")),
                entity("RG-B", type = "region", name = "Xibei", memberIds = listOf("CN-SC")),
            ),
        )
        assertEquals(
            listOf(ValidationError.RegionCoverage("CN-SC", 2), ValidationError.RegionCoverage("CN-HB", 0)),
            errors,
        )
    }

    // --- Noms en alphabet latin ---

    @Test
    fun chineseCharactersAsPinyin_areRejected() {
        assertEquals(listOf(ValidationError.InvalidName("CN-SC", "四川")), errorsOf(fileWith(entity("CN-SC", name = "四川"))))
    }

    @Test
    fun toneMarks_areRejected() {
        assertEquals(listOf(ValidationError.InvalidName("CN-SC", "Sìchuān")), errorsOf(fileWith(entity("CN-SC", name = "Sìchuān"))))
    }

    @Test
    fun blankName_isRejected() {
        assertEquals(listOf(ValidationError.InvalidName("CN-SC", "  ")), errorsOf(fileWith(entity("CN-SC", name = "  "))))
    }

    @Test
    fun apostrophesSpacesAndHyphens_areAccepted() {
        assertValid(
            fileWith(
                entity("A", name = "Xi'an"),
                entity("B", name = "Nei Mongol"),
                entity("C", name = "Hong-Kong"),
            ),
        )
    }

    // --- Territoires disputés ---

    @Test
    fun disputedIslandsWithIsoClaimants_areAccepted() {
        val islands = """{"id":"IS-A","type":"island_group","name":"Iles Paracel","claimedBy":["CN","TW","VN"],"sourceId":"s"}"""
        assertValid(fileWith(islands))
    }

    @Test
    fun claimantThatIsNotAnIsoCode_isRejected() {
        val islands = """{"id":"IS-A","type":"island_group","name":"Iles Paracel","claimedBy":["CN","Vietnam"],"sourceId":"s"}"""
        assertEquals(listOf(ValidationError.InvalidClaimant("IS-A", "Vietnam")), errorsOf(fileWith(islands)))
    }

    @Test
    fun islandGroupAttachedToACountry_isRejected() {
        val islands = """{"id":"IS-A","type":"island_group","name":"Iles Paracel","parentId":"CN","sourceId":"s"}"""
        assertEquals(listOf(ValidationError.InvalidParent("IS-A", "CN")), errorsOf(fileWith(islands)))
    }

    @Test
    fun riverLengths_areChecked() {
        fun river(lengths: String) = """{"id":"RV-A","type":"major_river","name":"Huang He",$lengths"sourceId":"s"}"""
        val valid = (GeoCatalogLoader.load(fileWith(river(""""lengthKm":4350,"lengthInChinaKm":2139,"""))) as LoadResult.Success).catalog["RV-A"]!!
        assertEquals(4350 to 2139, valid.lengthKm to valid.lengthInChinaKm)
        val invalid = listOf(""""lengthKm":0,""", """"lengthKm":100,"lengthInChinaKm":200,""", """"lengthInChinaKm":200,""")
        invalid.forEach { assertEquals(it, listOf(ValidationError.InvalidLength("RV-A")), errorsOf(fileWith(river(it)))) }
    }

    @Test
    fun knownAs_allowsAccents_butNameDoesNot() {
        val river = """{"id":"RV-A","type":"major_river","name":"Lancang Jiang","knownAs":"Mékong","sourceId":"s"}"""
        val entity = (GeoCatalogLoader.load(fileWith(river)) as LoadResult.Success).catalog["RV-A"]!!
        assertEquals("Mékong / Lancang Jiang", entity.displayName)
        assertEquals("Mékong", entity.shortName)
        val accentedName = """{"id":"RV-A","type":"major_river","name":"Lancang Jiāng","sourceId":"s"}"""
        assertEquals(listOf(ValidationError.InvalidName("RV-A", "Lancang Jiāng")), errorsOf(fileWith(accentedName)))
    }

    @Test
    fun knownAsThatRepeatsTheNameOrUsesHanzi_isRejected() {
        fun river(known: String) = """{"id":"RV-A","type":"major_river","name":"Taiwan","knownAs":"$known","sourceId":"s"}"""
        listOf("Taïwan", "澜沧江", "").forEach {
            assertEquals(it, listOf(ValidationError.InvalidKnownAs("RV-A", it)), errorsOf(fileWith(river(it))))
        }
    }

    @Test
    fun pinyinTonesInHanzi_isRejected() {
        val river = """{"id":"RV-A","type":"major_river","name":"Chang Jiang","pinyinTones":"长江","sourceId":"s"}"""
        assertEquals(listOf(ValidationError.InvalidPinyinTones("RV-A", "长江")), errorsOf(fileWith(river)))
    }

    @Test
    fun lakeArea_isCheckedAndReservedToLakes() {
        fun lake(area: String) = """{"id":"CN-A","type":"province","name":"Anhui","parentId":"CN","sourceId":"s"},{"id":"LK-A","type":"lake","name":"Chao Hu","parentId":"CN-A","areaKm2":$area,"sourceId":"s"}"""
        val valid = (GeoCatalogLoader.load(fileWith(lake("775"))) as LoadResult.Success).catalog["LK-A"]!!
        assertEquals(775.0, valid.areaKm2!!, 0.0)
        assertEquals(listOf(ValidationError.InvalidArea("LK-A")), errorsOf(fileWith(lake("0"))))
        val islands = """{"id":"IS-A","type":"island_group","name":"Iles Paracel","areaKm2":10,"sourceId":"s"}"""
        assertEquals(listOf(ValidationError.InvalidArea("IS-A")), errorsOf(fileWith(islands)))
    }

    @Test
    fun lakeOutsideAProvince_isRejected() {
        val lake = """{"id":"LK-A","type":"small_lake","name":"Xi Hu","sourceId":"s"}"""
        assertEquals(listOf(ValidationError.MissingParent("LK-A")), errorsOf(fileWith(lake)))
    }

    @Test
    fun lengthOnSomethingOtherThanARiver_isRejected() {
        val islands = """{"id":"IS-A","type":"island_group","name":"Iles Paracel","lengthKm":10,"sourceId":"s"}"""
        assertEquals(listOf(ValidationError.InvalidLength("IS-A")), errorsOf(fileWith(islands)))
    }
}
