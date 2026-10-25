# Vérification des fiches « En savoir plus »

Faite le 2026-10-25, à la demande de l'auteur : contrôler avec des sources citables tout ce que Claude a écrit de mémoire.

## Méthode

- Chaque phrase d'une fiche est comparée à une source : Wikipédia (anglais ou chinois, révision citée), liste officielle de l'UNESCO, sites officiels.
- Ce qui est confirmé est gardé, éventuellement précisé ; ce qui est faux est corrigé ; ce qui n'est trouvé dans aucune source est **retiré**, même si c'est probablement vrai. On ne remplace pas une affirmation retirée par une autre non vérifiée.
- Une légende est présentée comme telle (« selon la légende », conditionnel) ; il suffit qu'une source atteste qu'on la raconte.
- Une fiche entièrement vérifiée passe à `"reviewed": true` : l'application n'affiche plus « à vérifier ».
- Les révisions Wikipédia sont notées entre parenthèses (identifiant de révision) : `en:Qinghai Lake (1371829846)` se consulte à `https://en.wikipedia.org/w/index.php?oldid=1371829846`.

## Avancement

| Lot | Fiches | État |
|---|---:|---|
| Lacs et parcs (version 2) | 40 | **Vérifiées** le 2026-10-25 |
| Statuts et années des parcs (`parks.json`) | 16 | **Vérifiés** le 2026-10-25 (Zhangye : géoparc mondial en 2020, pas 2019) |
| Provinces | 34 | **Vérifiées** le 2026-10-25 |
| Villes | 40 | **Vérifiées** le 2026-10-25 |
| Cours d'eau | 27 | **Vérifiées** le 2026-10-25 |
| Montagnes | 37 | **Vérifiées** le 2026-10-25 |

## Statuts des parcs

| Parc | Statut | Source |
|---|---|---|
| Les 5 parcs nationaux | Créés en octobre 2021 | [gov.cn, 2025](https://english.www.gov.cn/news/202508/11/content_WS6899a99ec6d0868f4e8f4c16.html), en:List of national parks of China |
| Patrimoine mondial | Jiuzhaigou, Huanglong, Wulingyuan 1992 ; Lushan 1996 ; Wuyi 1999 ; Shilin 2007 et Guilin (Karst de Chine du Sud, phase II) 2014 ; Sanqingshan 2008 ; Danxia 2010 ; Fanjingshan 2018 ; Xi Hu 2011 | [Liste officielle de l'UNESCO](https://whc.unesco.org/en/list/xml/), fiches 637, 638, 640, 778, 911, 1248, 1292, 1335, 1559, 1334 |
| Géoparcs mondiaux UNESCO | Danxiashan, Lushan, Shilin, Zhangjiajie 2004 ; Sanqingshan 2012 (non affiché) ; Zhangye 2020 | en:List of geoparks in Asia (1355160153) ; Zhangye : [site du géoparc](https://www.zhangyegeopark.cn/en/h-nd-414.html), [gov.cn, 2020](https://english.www.gov.cn/news/videos/202007/09/content_WS5f06b828c6d05a0f897070c9.html) |
| Zones panoramiques nationales | Jiuzhaigou-Huanglong, Wulingyuan, Guilin-Lijiang, Lushan, Sanqingshan | zh:国家级风景名胜区 (95106563) |
| Réserves naturelles nationales | Fanjingshan (1978), Kanas | en:Fanjingshan (1369133591), zh:喀纳斯湖 |

## Lacs et parcs : sources et corrections

- **LK-QINGHAI** — sources : en:Qinghai Lake (1371829846), zh:日月山 (94421395), zh:倒淌河 (91955746), en:Tour of Qinghai Lake (1374204421). Corrections : Légende précisée (miroir brisé, larmes = Daotang He) ; oies à tête barrée non confirmées, remplacées par le sanctuaire des îles aux Oiseaux.
- **LK-POYANG** — sources : en:Poyang Lake (1376144256), zh:鄱阳湖 (92186273). Corrections : Légende de la « montagne-chaussure » retirée (non trouvée) ; « presque toutes les grues » corrigé en « jusqu'à 90 % ».
- **LK-DONGTING** — sources : en:Dongting Lake (1380997495), zh:斑竹 (91816887), en:Yueyang Tower (1364686164). Corrections : Légende complétée par l'origine du nom (« cour de la grotte »).
- **LK-TAIHU** — sources : en:Lake Tai (1380994503). Corrections : « Privé Wuxi d'eau potable » non confirmé, remplacé par les mesures prises ; légende localisée au lac Li.
- **LK-HONGZE** — sources : en:Hongze Lake (1366028789), zh:无支祁 (94515873). Corrections : « Fond plus haut que la plaine » non confirmé, retiré ; tombeaux Ming ajoutés.
- **LK-CHAOHU** — sources : en:Chao Lake (1365915928). Corrections : Légende corrigée (dragon blanc, mère et fille changées en deux îles).
- **LK-GAOYOU** — sources : zh:高邮湖 (79509755), en:Gaoyou (1379154197). Corrections : « Œufs à double jaune » corrigé en « œufs de cane salés ».
- **LK-WEISHAN** — sources : zh:微山湖 (94296175). Corrections : Le nom du lac tiré de Weizi non confirmé : présenté comme simple fait (tombeau) ; chanson remplacée par le roman.
- **LK-HONGHU** — sources : en:Hong Lake (1366028760), zh:洪湖赤卫队 (歌剧) (86324490). Corrections : « Poissons » non confirmé, retiré.
- **LK-HULUN** — sources : zh:呼倫湖 (94706863), en:Hulun Lake (1366029569). Corrections : Légende des deux amants non trouvée, remplacée par l'origine des noms ; « berceau des Xianbei » non confirmé, retiré.
- **LK-XINGKAI** — sources : en:Lake Khanka (1374241123). Corrections : « Un quart chinois » et le poisson « baiyu » non confirmés, retirés.
- **LK-NAMCO** — sources : zh:纳木错 (94330020), en:Namtso (1380524507). Corrections : « Lac salé », union avec le mont Nyenchen Tanglha et « bruit de tonnerre » non confirmés, retirés.
- **LK-SILING** — sources : zh:色林错 (91956217), en:Siling Lake (1376090307). Corrections : Légende du démon et cause glaciaire non confirmées, retirées ; date du dépassement corrigée (années 2000).
- **LK-YAMZHO** — sources : zh:羊卓雍错 (93373038), en:Yamdrok Lake (1366934504). Corrections : « Forme de scorpion » et « le Tibet deviendrait inhabitable » non confirmés, retirés.
- **LK-MAPAM** — sources : en:Lake Manasarovar (1376827844), en:Lake Rakshastal (1366267862). Corrections : « Cent vies » non confirmé, retiré ; le Rakshastal rattaché à Ravana.
- **LK-GYARING** — sources : zh:扎陵湖 (91955855). Corrections : Légende de la princesse Wencheng déplacée au lac Ngoring (ancien lac Bohai).
- **LK-NGORING** — sources : zh:鄂陵湖 (94425369), zh:文成公主 (93357463). Corrections : Monument « à tête de yak » et « plus grand lac d'eau douce du plateau » non confirmés, retirés.
- **LK-BOSTEN** — sources : en:Bosten Lake (1365910153). Corrections : Roselières et papeteries non confirmées, retirées.
- **LK-EBINUR** — sources : en:Ebi Lake (1365990205). Corrections : Tempêtes de sel et rétrécissement non confirmés, retirés.
- **LK-DANJIANGKOU** — sources : en:Danjiangkou Reservoir (1365986176), zh:丹江口水库 (94413291). Corrections : « Achevé en 1973, rehaussé en 2013 » corrigé (rempli 1970-1973, barrage relevé 2005-2009, nouveau niveau atteint en 2014).
- **LK-QIANDAO** — sources : en:Qiandao Lake (1366613555). Corrections : Deuxième ville engloutie non confirmée ; ajout de l'élevage d'esturgeons.
- **LK-XIHU** — sources : en:West Lake (1366781769). Corrections : Date de la digue corrigée (1089) ; Corée ajoutée.
- **LK-DIANCHI** — sources : en:Dian Lake (1373293163), zh:西山 (昆明) (92475070), zh:红嘴鸥 (94327219). Corrections : Légende de la princesse non trouvée, remplacée par la forme de la montagne ; modèle du Palais d'été ajouté.
- **LK-ERHAI** — sources : en:Erhai Lake (1368537035), zh:大理市 (94999791). Corrections : Erreur corrigée : « en forme d'oreille (er) » est faux, le caractère ne signifie pas « oreille ».
- **PA-SANJIANGYUAN** — sources : zh:三江源 (95247725), en:Sanjiangyuan (1365771605), en:Epic of King Gesar (1377615674), en:Tibetan antelope (1381116353). Corrections : « Château d'eau de la Chine » et gardes anciens bergers non confirmés, retirés ; superficie ajoutée.
- **PA-PANDA** — sources : en:Giant Panda National Park (1375722201), en:Giant panda (1381023700), Smithsonian Folklife, « Where'd You Get Those Eyes? The Folklore of Pandas ». Corrections : « Jusqu'à une trentaine de kilos » corrigé en 9 à 14 kg ; superficie et 80 % ajoutés.
- **PA-HUBAO** — sources : en:Northeast China Tiger and Leopard National Park (1362882822), en:Amur leopard (1379563235), zh:虎 (95171968). Corrections : « Quelques centaines au plus » précisé (environ 130 en 2023).
- **PA-HAINAN** — sources : zh:海南热带雨林国家公园 (93654516), en:Hainan black crested gibbon (1356939433). Corrections : « Plus grande forêt tropicale d'un seul tenant » non confirmé, retiré ; effectif précisé.
- **PA-WUYISHAN** — sources : en:Wuyi Mountains (1375809115), zh:武夷山脉 (74874420), en:Da Hong Pao (1364707541). Corrections : Légende de Peng Wu et Peng Yi non trouvée, remplacée par Wuyi Jun ; arrêt de la récolte en 2006 non confirmé, remplacé par la vente de 2005.
- **PA-JIUZHAIGOU** — sources : en:Jiuzhaigou (1371997863). Corrections : Légende du miroir brisé en 108 morceaux non trouvée, remplacée par la version de la source ; durée de fermeture corrigée (pas deux ans).
- **PA-HUANGLONG** — sources : en:Huanglong Scenic and Historic Interest Area (1365736053), en:Pamukkale (1365738737). Corrections : Légende du dragon aidant Yu le Grand non trouvée, remplacée ; « plus de 3 000 m » non confirmé, retiré.
- **PA-WULINGYUAN** — sources : en:Wulingyuan (1365860904), en:Zhangjiajie (1378504955), en:Tianzi Mountain (1379701433), en:Bailong Elevator (1327532288). Corrections : Renommage « montagne Hallelujah » en 2010 non confirmé, retiré.
- **PA-LIJIANG** — sources : en:Li River (1367592741), zh:象鼻山 (94013525), en:Elephant Trunk Hill (1304034033). Corrections : Légende corrigée (l'éléphant fut puni, il n'était pas « resté aider les paysans »).
- **PA-SHILIN** — sources : en:Stone Forest (1373824237), zh:石林风景名胜区 (90834852). Corrections : « Sud-est » corrigé en « est » ; Ashima « changée en rocher » non confirmé, retiré.
- **PA-FANJINGSHAN** — sources : en:Fanjingshan (1369133591), zh:梵净山 (94743065). Corrections : « Un temple de chaque côté » non confirmé, retiré.
- **PA-KANAS** — sources : en:Kanas Lake (1366035524), zh:喀纳斯湖 (94762901). Corrections : Maisons en rondins non confirmées, retirées ; observation de 1985 précisée.
- **PA-DANXIA** — sources : en:Mount Danxia (1369555103), zh:丹霞山 (95009691). Corrections : Hauteur de 28 m et culte de la fertilité non confirmés, retirés ; origine du nom précisée.
- **PA-LUSHAN** — sources : en:Mount Lu (1375480800), en:Lushan Conference (1360761349). Corrections : Le vers de Su Shi passe de « légende » à anecdote ; « capitale d'été » ajoutée.
- **PA-SANQINGSHAN** — sources : en:Mount Sanqing (1362860744). Corrections : Pic de la « Déesse » de 86 m non confirmé, retiré.
- **PA-ZHANGYE** — sources : en:Zhangye National Geopark (1362954203). Corrections : Tournage du film de Zhang Yimou non confirmé, retiré.

Bilan du lot : 40 fiches, dont plus de la moitié corrigées ; une erreur franche (Er Hai n'est pas « en forme d'oreille »), plusieurs chiffres rectifiés (bambou du panda, date de la digue Su, rehaussement de Danjiangkou), et une dizaine de légendes ou détails introuvables retirés ou remplacés par la version attestée.

## Population et superficie des unités provinciales

- **Population** : recensement chinois de 2020 (septième recensement), d'après le tableau « Census data » de en:List of Chinese provincial-level divisions by population (1359277690), qui cite le Bureau national des statistiques. Les 31 valeurs de la Chine continentale étaient exactes. Hong Kong et Macao : 2021. Taïwan : **corrigé**, 23,4 millions en avril 2025 d'après en:Demographics of Taiwan (1381597217), au lieu d'une estimation 2023.
- **Superficie** : une seule source pour toutes les provinces, le tableau de en:Provincial-level divisions of China (1376622017), arrondi à la centaine de km². Cinq valeurs s'écartaient de plus de 2 % et ont été **corrigées** : Gansu (457 400 au lieu de 425 800), Jiangsu (99 900 au lieu de 107 200), Yunnan, Hainan, Tianjin. Les chiffres officiels chinois diffèrent parfois de ce tableau (le Jiangsu annonce 107 200 km² avec ses eaux) : l'écart est signalé, le tableau est retenu pour la cohérence. Hong Kong (1 110 km²), Macao (33 km²) et Taïwan (36 200 km²) d'après leurs articles.

## Provinces : sources et corrections

- **CN-BJ** — sources : en:Beijing (1381790538), zh:北京市 (94723638), en:Provincial-level divisions of China (1376622017). Corrections : Le pékinois « base de la langue standard » n'est pas dans la source, retiré.
- **CN-TJ** — sources : en:Tianjin (1379319910), zh:天津市 (95170779). Corrections : Précisions sourcées (Airbus, concession italienne).
- **CN-HE** — sources : en:Hebei (1381901070), zh:河北省 (94411813). Corrections : « Jin dans l'ouest » et « la muraille rejoint la mer » non confirmés, retirés ; anecdote de Shanhaiguan remplacée par l'épisode de 1644.
- **CN-SX** — sources : en:Shanxi (1376852277), zh:山西省 (95262847). Corrections : « Proche du mandarin » corrigé (le jin n'est pas du mandarin) ; remparts Ming de Pingyao non confirmés ; « deux premières régions productrices » remplacé par les réserves.
- **CN-NM** — sources : en:Inner Mongolia (1376258301), zh:内蒙古自治区 (94774989). Corrections : « Première région autonome », « 17 % », Bayan Obo, « 2 400 km » et « écriture verticale » non confirmés, remplacés par des faits sourcés.
- **CN-LN** — sources : en:Liaoning (1377123945), zh:辽宁省 (94254673). Corrections : Raviolis de Shenyang et « l'un des deux palais conservés » non confirmés, reformulés.
- **CN-JL** — sources : en:Jilin (1380405089), zh:吉林省 (94451583). Corrections : FAW, soja, forêts et lac « gelé une grande partie de l'année » non confirmés, retirés.
- **CN-HL** — sources : en:Heilongjiang (1377669299), zh:黑龙江省 (94742878). Corrections : Chemin de fer de l'Est chinois, guobaorou et année 1969 non confirmés, retirés.
- **CN-SH** — sources : en:Shanghai (1381637672), zh:上海市 (95221788). Corrections : « Pudong surtout agricole en 1990 » et « automobile, semi-conducteurs » non confirmés, retirés.
- **CN-JS** — sources : en:Jiangsu (1380736753), zh:江苏省 (94254714), en:Nanjing (1380900031). Corrections : Dialectes précisés ; point culminant chiffré (625 m).
- **CN-ZJ** — sources : en:Zhejiang (1380197022), zh:浙江省 (95233933). Corrections : Dates « 1138-1279 » et vin de Shaoxing non confirmés ; « l'un des plus grands marchés du monde » adouci.
- **CN-FJ** — sources : en:Fujian (1380791229), zh:福建省 (94993128). Corrections : Tieguanyin et chaussures non confirmés ; Quanzhou reformulé d'après l'inscription UNESCO.
- **CN-JX** — sources : en:Jiangxi (1381841069), zh:江西省 (94609543), en:Poyang Lake (1376144256), en:Nanchang (1377782255). Corrections : « Depuis plus de mille ans », base de Mao en 1927 et terres rares non confirmés, reformulés. Plat précisé.
- **CN-SD** — sources : en:Shandong (1381551280), zh:山东省 (94758528). Corrections : Fin de la concession en 1914 non confirmée ; delta précisé.
- **CN-HI** — sources : en:Hainan (1381410331), zh:海南省 (94686076), en:Wenchang Space Launch Site (1370533635). Corrections : Précisions sourcées (Hlai, régime douanier, Longue Marche 5).
- **CN-CQ** — sources : en:Chongqing (1381846454), zh:重庆市 (95224605), en:Liziba station (1367577125). Corrections : Dates corrigées (1937-1945 au lieu de 1938-1946) ; « métro » corrigé en « monorail ».
- **CN-GX** — sources : en:Guangxi (1381843749), zh:广西壮族自治区 (95031320), zh:灵渠 (94273972), en:Li River (1367592741). Corrections : Erreur corrigée : le canal de Lingqu n'est plus « en service » (écluses abandonnées en 1956).
- **CN-SC** — sources : en:Sichuan (1381848153), zh:四川省 (94610329), whc.unesco.org/en/list/779. Corrections : Date des bronzes (1200 av. J.-C.), « fonctionne depuis le IIIe siècle » et « plus grande statue taillée dans la roche » non confirmés, retirés.
- **CN-GZ** — sources : en:Guizhou (1381844157), zh:贵州省 (94254881). Corrections : Dicton corrigé (« trois li », pas « trois pieds »).
- **CN-YN** — sources : en:Yunnan (1381718738), zh:云南省 (95233778). Corrections : « 25 minorités reconnues » et thé pu-erh non confirmés ; Nanzhao ajouté.
- **CN-XZ** — sources : en:Tibet Autonomous Region (1381565795), zh:西藏自治区 (95242335), en:Potala Palace (1372535590). Corrections : « Élevage du yak, mines » non confirmés ; part du budget chiffrée ; altitude rattachée au plateau.
- **CN-SN** — sources : en:Shaanxi (1377682762), zh:陕西省 (94254951), en:Yan'an (1378291234), en:Biangbiang noodles (1381232802). Corrections : Dates de Yan'an corrigées (1935-1947 au lieu de 1937-1947) ; « plus de 50 traits » précisé (58).
- **CN-GS** — sources : en:Gansu (1377824250), zh:甘肃省 (94494827), en:Mogao Caves (1373712116). Corrections : Pétrole de Yumen, « près de mille ans » (environ 900) et « plus de 1 600 km » non confirmés ; superficie corrigée (457 400 km² au lieu de 425 800).
- **CN-QH** — sources : en:Qinghai (1378487652), zh:青海省 (94494944). Corrections : Plat (mouton, yaourt de yak) et potasse du lac Qarhan non confirmés ; plat remplacé par le mian pian, attesté par en:Xining (1378847127).
- **CN-NX** — sources : en:Ningxia (1378231235), zh:宁夏回族自治区 (95137512), en:Western Xia mausoleums (1366157511). Corrections : « Pyramides de Chine » non confirmé, remplacé par l'inscription UNESCO de 2025.
- **CN-XJ** — sources : en:Xinjiang (1381904750). Corrections : Profondeur corrigée (−155 m au lieu de −154) ; plat confirmé ensuite par en:Uyghur cuisine (1379091277) : polo, laghman, brochettes.
- **CN-HK** — sources : en:Hong Kong (1381597913). Corrections : Précisions sourcées (troisième place financière, record de gratte-ciel).
- **CN-MO** — sources : en:Macau (1380630381). Corrections : « Premier comptoir européen durable » non confirmé, retiré ; terres gagnées chiffrées.
- **CN-TW** — sources : en:Taiwan (1380084849), en:Demographics of Taiwan (1381597217), en:Taipei (1381377799). Corrections : Population mise à jour (23,4 millions en 2025) ; « plus haut que le mont Fuji » et dates de Taipei 101 remplacés par des faits sourcés.
- **CN-AH** — sources : en:Anhui (1378231344), zh:安徽省 (95161873), en:Huangshan (1377541393). Corrections : « Véhicules électriques, électroménager » non confirmés, remplacés par Chery.
- **CN-HA** — sources : en:Henan (1381847851), zh:河南省 (94716870), en:Zhengzhou (1380360086), zh:郑州市 (94297652), en:Longmen Grottoes (1366154989). Corrections : Bouillon de mouton du huimian non confirmé ; « dizaines de milliers de statues » précisé (100 000).
- **CN-HB** — sources : en:Hubei (1381840779), zh:湖北省 (94254824), en:Wuhan (1379681425), en:Three Gorges Dam (1381450956), en:Yeren (1371691330). Corrections : Précisions sourcées.
- **CN-HN** — sources : en:Hunan (1381834779), zh:湖南省 (95234074), en:Tianzi Mountain (1379701433). Corrections : Corps conservé de Mawangdui, télévision et tête de poisson non confirmés, retirés.
- **CN-GD** — sources : en:Guangdong (1381843446), zh:广东省 (94947556), en:Guangzhou (1381090861), en:Shenzhen (1381454144). Corrections : Erreur corrigée : Shenzhen comptait 30 000 habitants en 1980, pas 300 000 ; char siu non confirmé.

## Villes : sources et corrections

- **CT-SHANGHAI** — sources : en:Shanghai (1381637672). Corrections : « Années 1920-1930 » remplacé par « début du XXe siècle » ; longueur du métro ajoutée.
- **CT-BEIJING** — sources : en:Beijing (1381790538). Corrections : Confirmé sans changement.
- **CT-SHENZHEN** — sources : en:Shenzhen (1381454144). Corrections : Population de 1980 ajoutée.
- **CT-GUANGZHOU** — sources : en:Guangzhou (1381090861), zh:广州市 (94906060). Corrections : « Depuis 1957 » et surnom « taille fine » non confirmés, retirés.
- **CT-CHENGDU** — sources : en:Chengdu (1381569564). Corrections : Anecdote chiffrée.
- **CT-TIANJIN** — sources : en:Tianjin (1379319910), zh:天津市 (95170779). Corrections : Première ligne à grande vitesse (2008) ajoutée.
- **CT-WUHAN** — sources : en:Wuhan (1379681425). Corrections : Précisions sourcées.
- **CT-DONGGUAN** — sources : en:Dongguan (1380992959). Corrections : Meubles et nom de Lin Zexu non confirmés, retirés.
- **CT-CHONGQING** — sources : en:Chongqing (1381846454), zh:重庆市 (95224605). Corrections : Précisions sourcées.
- **CT-XIAN** — sources : en:Xi'an (1381048170), zh:西安市 (94552795). Corrections : Longueur des remparts (14 km) non confirmée ; « plus grande ville du monde » rattachée à la fondation de 582.
- **CT-HANGZHOU** — sources : en:Hangzhou (1380736744). Corrections : Confirmé, précisé.
- **CT-FOSHAN** — sources : en:Foshan (1378097881), zh:佛山市 (95243470). Corrections : Ip Man et danse du lion non confirmés, remplacés.
- **CT-NANJING** — sources : en:Nanjing (1380900031). Corrections : Erreur corrigée : en 1368, la « capitale du Nord » était Kaifeng, pas Pékin.
- **CT-SHENYANG** — sources : en:Shenyang (1380805485). Corrections : Confirmé sans changement.
- **CT-ZHENGZHOU** — sources : en:Zhengzhou (1380360086). Corrections : Distances et âges corrigés (90 km au lieu de 80 ; 3 600 ans au lieu de 3 500).
- **CT-QINGDAO** — sources : en:Qingdao (1380886153). Corrections : Jetée de Zhanqiao non confirmée, retirée.
- **CT-SUZHOU** — sources : en:Suzhou (1381717154). Corrections : Attribution à Marco Polo ajoutée.
- **CT-JINAN** — sources : en:Jinan (1381788006). Corrections : Précision ajoutée.
- **CT-CHANGSHA** — sources : en:Changsha (1381846979). Corrections : Reformulé d'après la source.
- **CT-KUNMING** — sources : en:Kunming (1380509744), en:Stone Forest (1373824237). Corrections : Distance de Shilin corrigée (90 km au lieu de 80).
- **CT-HARBIN** — sources : en:Harbin (1380817422), zh:哈尔滨市 (94538615). Corrections : « Souvent sous −30 °C » corrigé (souvent sous −20 °C, parfois −30 °C).
- **CT-SHIJIAZHUANG** — sources : en:Shijiazhuang (1378810575). Corrections : Date du pont (VIIe siècle) non confirmée, retirée.
- **CT-HEFEI** — sources : en:Hefei (1381551394). Corrections : Précisions sourcées.
- **CT-DALIAN** — sources : en:Dalian (1375012995). Corrections : « L'une des plus grandes places d'Asie » non confirmé, retiré.
- **CT-XIAMEN** — sources : en:Xiamen (1381565830), zh:厦门市 (95225009). Corrections : « Interdite aux voitures » non confirmé, retiré.
- **CT-NANNING** — sources : en:Nanning (1379796575). Corrections : Date ajoutée.
- **CT-CHANGCHUN** — sources : en:Changchun (1381463039). Corrections : « Premiers studios de cinéma de la République populaire » non confirmé, remplacé.
- **CT-TAIYUAN** — sources : en:Taiyuan (1380211590). Corrections : « Acier » et « statues Song » non confirmés, reformulés.
- **CT-GUIYANG** — sources : en:Guiyang (1375960134). Corrections : Reformulé d'après la source.
- **CT-WUXI** — sources : en:Wuxi (1376423841), en:Lake Tai (1380994503). Corrections : Confirmé.
- **CT-URUMQI** — sources : en:Ürümqi (1381344374). Corrections : Source Guinness ajoutée.
- **CT-ZHONGSHAN** — sources : en:Zhongshan (1381348220). Corrections : Date du changement de nom ajoutée.
- **CT-SHANTOU** — sources : en:Shantou (1375924828). Corrections : Quartier des qilou non confirmé, retiré ; diaspora chiffrée.
- **CT-NINGBO** — sources : en:Ningbo (1380758455). Corrections : Précisé (1516).
- **CT-FUZHOU** — sources : en:Fuzhou (1381695057). Corrections : Confirmé sans changement.
- **CT-NANCHANG** — sources : en:Nanchang (1377782255). Corrections : Dates ajoutées.
- **CT-CHANGZHOU** — sources : en:Changzhou (1378845281). Corrections : Hauteur de la pagode (154 m) et « 2 500 ans » non confirmés, retirés.
- **CT-LANZHOU** — sources : en:Lanzhou (1381706932). Corrections : Précisé.
- **CT-HONGKONG** — sources : en:Hong Kong (1381597913). Corrections : Date ajoutée.
- **CT-TAIPEI** — sources : en:Taipei (1381377799). Corrections : « Collections impériales emportées en 1949 » non confirmé dans la source, reformulé.

## Cours d'eau : sources et corrections

- **RV-CHANGJIANG** — sources : en:Yangtze (1379055480), en:Three Gorges Dam (1381450956). Corrections : « Plus long d'Asie » remplacé par « troisième du monde » ; longueur du lac précisée (660 km).
- **RV-HUANGHE** — sources : en:Yellow River (1381531535). Corrections : « Quantité record » remplacé par une donnée chiffrée.
- **RV-HEILONGJIANG** — sources : en:Amur (1380040663). Corrections : « Gelé une grande partie de l'hiver » non trouvé dans la source, remplacé.
- **RV-ZHUJIANG** — sources : en:Pearl River (1368001301), en:Pearl River Delta (1381715193). Corrections : Anecdote chiffrée.
- **RV-LANCANGJIANG** — sources : en:Mekong (1374952293). Corrections : Reformulé d'après la source.
- **RV-NUJIANG** — sources : en:Salween River (1381107461). Corrections : Origine du nom précisée.
- **RV-YARLUNGZANGBO** — sources : en:Yarlung Tsangpo (1372301789). Corrections : « L'un des plus profonds » corrigé en « le plus profond ».
- **RV-HUAIHE** — sources : en:Huai River (1378783426), zh:淮河 (94005878). Corrections : « Blé au nord, riz au sud » non confirmé, retiré.
- **RV-LIAOHE** — sources : en:Liao River (1380700183). Corrections : « Le Liao apaisé » et « plage rouge de Panjin » non confirmés, retirés ; le nom vient de la région, pas l'inverse.
- **RV-HAIHE** — sources : en:Hai River (1367432732), zh:海河 (93198103). Corrections : Précisé.
- **RV-TARIMHE** — sources : en:Tarim River (1379313053). Corrections : Précisé.
- **RV-EERQISIHE** — sources : en:Irtysh (1374512227), zh:额尔齐斯河 (93192547). Corrections : Record du plus long affluent ajouté.
- **RV-HANJIANG** — sources : en:Han River (Hubei and Shaanxi) (1366532437), en:Wuhan (1379681425). Corrections : « Alimente Pékin » reformulé ; lien avec les Han précisé.
- **RV-JIALINGJIANG** — sources : en:Jialing River (1295524348). Corrections : « Mélange visible des eaux à Chongqing » non confirmé, reformulé.
- **RV-MINJIANG** — sources : en:Min River (Sichuan) (1294426103). Corrections : « Face au confluent du Min et du Dadu » non confirmé, reformulé.
- **RV-DADUHE** — sources : en:Dadu River (1363562425). Corrections : Année 1935 non confirmée dans la source, retirée ; anecdote ajoutée.
- **RV-YALONGJIANG** — sources : en:Yalong River (1368124151), en:Jinping-I Dam (1369025605). Corrections : « L'un des plus hauts » corrigé en « le plus haut barrage-voûte ».
- **RV-WUJIANG** — sources : en:Wu River (Yangtze tributary) (1370645705), en:Long March (1381146417). Corrections : Précisé.
- **RV-XIANGJIANG** — sources : en:Xiang River (1376272271), en:Hunan (1381834779). Corrections : « Traverse Changsha » non confirmé, retiré.
- **RV-YUANJIANG** — sources : en:Yuan River (1346015335). Corrections : Bourg de Fenghuang non confirmé, retiré.
- **RV-GANJIANG** — sources : en:Gan River (1248918321). Corrections : Langue gan ajoutée.
- **RV-WEIHE** — sources : en:Wei River (1371690488). Corrections : « Xi'an près de ses rives » remplacé par une donnée sourcée.
- **RV-SONGHUAJIANG** — sources : en:Songhua River (1374562724). Corrections : Givre de Jilin non confirmé, remplacé.
- **RV-WUSULIJIANG** — sources : zh:乌苏里江 (93905371). Corrections : Confirmé.
- **RV-DONGJIANG** — sources : en:Hong Kong (1381597913). Corrections : Shenzhen non confirmé ; part chiffrée.
- **RV-BEIJIANG** — sources : en:Bei River (1365395374), zh:北江 (85546379). Corrections : « Grande route vers Canton » non confirmé, retiré.
- **RV-YONGDINGHE** — sources : en:Yongding River (1368124829), en:Marco Polo Bridge incident (1370745067). Corrections : Date de construction ajoutée.

## Montagnes : sources et corrections

- **MT-HIMALAYA** — sources : en:Himalayas (1379823424). Corrections : Formulation précisée.
- **MT-KARAKORAM** — sources : en:Karakoram (1378615604). Corrections : Record d'englacement ajouté.
- **MT-KUNLUN** — sources : zh:昆仑山脉 (92972845), en:Kunlun Mountains (1377657560). Corrections : Confirmé (2 500 km selon la Wikipédia chinoise, plus de 3 000 km selon l'anglaise).
- **MT-TIANSHAN** — sources : en:Tian Shan (1369727502). Corrections : Confirmé.
- **MT-ALTAI** — sources : en:Altai Mountains (1379861258). Corrections : Précisé.
- **MT-ALTUN** — sources : en:Altyn-Tagh (1368599623). Corrections : Yacks et ânes sauvages non confirmés, reformulé.
- **MT-QILIAN** — sources : en:Qilian Mountains (1357546961). Corrections : Précisé.
- **MT-QINLING** — sources : en:Qinling (1372790693). Corrections : Précisé et chiffré.
- **MT-DAXINGANLING** — sources : en:Greater Khingan (1379405723), zh:大兴安岭 (94814541). Corrections : « Parmi les plus vastes de Chine » non confirmé, reformulé.
- **MT-XIAOXINGANLING** — sources : en:Lesser Khingan (1369427704). Corrections : Forêts de pins de Corée non confirmées, remplacées.
- **MT-TAIHANG** — sources : en:Taihang Mountains (1381681620), en:Guoliang Tunnel (1349037197). Corrections : Dates précisées ; origine du nom du Shanxi ajoutée.
- **MT-LULIANG** — sources : en:Lüliang Mountains (1333855877). Corrections : Base de guérilla non confirmée, remplacée.
- **MT-YINSHAN** — sources : en:Yin Mountains (1325145230), zh:阴山 (94549808). Corrections : Gravures rupestres non confirmées, retirées.
- **MT-DABIE** — sources : en:Dabie Mountains (1377790834). Corrections : « Limite Nord-Sud » retiré (seule la ligne de partage des eaux est sourcée).
- **MT-DALOU** — sources : en:Dalou Mountains (1368790960), zh:娄山关 (92544886). Corrections : Mois précisé.
- **MT-NANLING** — sources : en:Nanling Mountains (1369641892). Corrections : Précisé.
- **MT-WUYI** — sources : en:Wuyi Mountains (1375809115). Corrections : Point culminant ajouté.
- **PK-QOMOLANGMA** — sources : en:Mount Everest (1381762057). Corrections : Date précisée.
- **PK-K2** — sources : en:K2 (1381911369). Corrections : Précisé.
- **PK-NAMCHABARWA** — sources : en:Namcha Barwa (1357725304). Corrections : Précisé.
- **PK-KONGUR** — sources : en:Kongur Tagh (1369422235). Corrections : « Pamir chinois » corrigé : c'est le plus haut de tout le Pamir.
- **PK-GONGGA** — sources : en:Mount Gongga (1369567084). Corrections : Précisé.
- **PK-TOMUR** — sources : en:Jengish Chokusu (1377901028). Corrections : Date ajoutée.
- **PK-NYAINQENTANGLHA** — sources : en:Nyenchen Tanglha Mountains (1355488421). Corrections : Distance ajoutée.
- **PK-KAILASH** — sources : en:Mount Kailash (1376804607). Corrections : Longueur du tour (52 km) non confirmée, retirée.
- **PK-GELADANDONG** — sources : en:Geladaindong Peak (1375875895). Corrections : « Source officielle » nuancé.
- **PK-ANYEMAQEN** — sources : en:Amne Machin (1368600142). Corrections : Chiffré.
- **PK-BOGDA** — sources : en:Heavenly Lake of Tian Shan (1367683418). Corrections : « Visible depuis Urumqi » non confirmé, retiré.
- **PK-YUSHAN** — sources : en:Yushan (mountain) (1380682941). Corrections : Chiffré.
- **PK-TAIBAI** — sources : en:Mount Taibai (1375419143). Corrections : Précisé.
- **PK-EMEI** — sources : en:Mount Emei (1374489224). Corrections : Précisé.
- **PK-WUTAI** — sources : en:Mount Wutai (1369587160). Corrections : Confirmé.
- **PK-CHANGBAI** — sources : en:Mount Paektu (1380367332). Corrections : « Des deux derniers millénaires » corrigé (5 000 ans).
- **PK-HUASHAN** — sources : en:Mount Hua (1369568765). Corrections : Reformulé d'après la source.
- **PK-HUANGSHAN** — sources : zh:黄山 (94378912), en:Huangshan (1377541393). Corrections : Dicton attribué à son auteur et cité exactement.
- **PK-WUZHI** — sources : en:Wuzhi Mountain (1375730132). Corrections : « D'après la forme de ses cinq pointes » non confirmé, remplacé.
- **PK-TAISHAN** — sources : en:Mount Tai (1381369463). Corrections : Nombre précisé.

## Bilan général

Les 178 fiches sont vérifiées. Erreurs franches corrigées, entre autres : Shenzhen comptait 30 000 habitants en 1980 (et non 300 000) ; en 1368, la « capitale du Nord » opposée à Nankin était Kaifeng, pas Pékin ; le canal de Lingqu n'est plus en service depuis 1956 ; Yan'an fut la base communiste dès 1935 ; le Kongur est le plus haut sommet de tout le Pamir ; Chongqing fut capitale de guerre de 1937 à 1945 ; Er Hai n'est pas « en forme d'oreille ». Les affirmations introuvables dans les sources ont été retirées plutôt que maintenues (détail fiche par fiche ci-dessus).
