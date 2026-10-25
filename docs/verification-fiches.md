# Vérification des fiches « En savoir plus »

Commencée le 2026-10-25, à la demande de l'auteur : contrôler avec des sources citables tout ce que Claude a écrit de mémoire.

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
| Provinces | 34 | À faire |
| Villes | 40 | À faire |
| Cours d'eau | 27 | À faire |
| Montagnes | 37 | À faire |

## Statuts des parcs

| Parc | Statut | Source |
|---|---|---|
| Les 5 parcs nationaux | Créés en octobre 2021 | [gov.cn, 2025](https://english.www.gov.cn/news/202508/11/content_WS6899a99ec6d0868f4e8f4c16.html), en:List of national parks of China |
| Patrimoine mondial | Jiuzhaigou, Huanglong, Wulingyuan 1992 ; Lushan 1996 ; Wuyi 1999 ; Shilin 2007 et Guilin (Karst de Chine du Sud, phase II) 2014 ; Sanqingshan 2008 ; Danxia 2010 ; Fanjingshan 2018 ; Xi Hu 2011 | [Liste officielle de l'UNESCO](https://whc.unesco.org/en/list/xml/), fiches 637, 638, 640, 778, 911, 1248, 1292, 1335, 1559, 1334 |
| Géoparcs mondiaux UNESCO | Danxiashan, Lushan, Shilin, Zhangjiajie 2004 ; Sanqingshan 2012 (non affiché) ; Zhangye 2020 | en:List of geoparks in Asia (1355160153) ; Zhangye : [site du géoparc](https://www.zhangyegeopark.cn/en/h-nd-414.html), [gov.cn, 2020](https://english.www.gov.cn/news/videos/202007/09/content_WS5f06b828c6d05a0f897070c9.html) |
| Zones panoramiques nationales | Jiuzhaigou-Huanglong, Wulingyuan, Guilin-Lijiang, Lushan, Sanqingshan | zh:国家级风景名胜区 (95106563) |
| Réserves naturelles nationales | Fanjingshan (1978), Kanas | en:Fanjingshan (1369133591), zh:喀纳斯湖 |

## Fiches vérifiées : sources et corrections

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
