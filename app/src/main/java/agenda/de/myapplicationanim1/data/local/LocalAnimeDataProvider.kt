package agenda.de.myapplicationanim1.data.local

import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.BroadcastInfo
import agenda.de.myapplicationanim1.data.model.ExternalLinkInfo

object LocalAnimeDataProvider {

    val allAnimes: List<Anime> = listOf(
        // ================= SEGUNDA =================
        Anime(
            id = 101,
            title = "Solo Leveling Season 2: Arise from the Shadow",
            titleEnglish = "Solo Leveling Season 2: Arise from the Shadow",
            titleRomaji = "Ore dake Level Up na Ken: Arise from the Shadow",
            titleJapanese = "俺だけレベルアップな件",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1915/136066.jpg",
            largeImageUrl = "https://cdn.myanimelist.net/images/anime/1915/136066.jpg",
            synopsis = "O caçador Sung Jinwoo enfrenta dungeons de rank S e expande seu exército de sombras para salvar a humanidade.",
            score = 8.9,
            episodes = 13,
            status = "RELEASING",
            aired = "2025-01-05",
            broadcast = BroadcastInfo(day = "Segunda", time = "13:30", timezone = "JST", stringFormat = "Segunda às 13:30"),
            airingDay = "Segunda",
            genres = listOf("Ação", "Fantasia", "Sobrenatural"),
            externalLinks = listOf(
                ExternalLinkInfo("Crunchyroll", "https://www.crunchyroll.com/solo-leveling"),
                ExternalLinkInfo("Netflix", "https://www.netflix.com")
            )
        ),
        Anime(
            id = 102,
            title = "Tower of God Season 2",
            titleEnglish = "Tower of God Season 2",
            titleRomaji = "Kami no Tou: Ouji no Kikan",
            titleJapanese = "神之塔 -Tower of God-",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1764/142988.jpg",
            largeImageUrl = "https://cdn.myanimelist.net/images/anime/1764/142988.jpg",
            synopsis = "Ja Wangnan tenta passar do 20º andar da torre enquanto novos mistérios e caçadores entram na disputa.",
            score = 7.8,
            episodes = 26,
            status = "RELEASING",
            aired = "2024-07-07",
            broadcast = BroadcastInfo(day = "Segunda", time = "23:00", timezone = "JST", stringFormat = "Segunda às 23:00"),
            airingDay = "Segunda",
            genres = listOf("Ação", "Aventura", "Fantasia"),
            externalLinks = listOf(ExternalLinkInfo("Crunchyroll", "https://www.crunchyroll.com"))
        ),
        Anime(
            id = 103,
            title = "Blue Box",
            titleEnglish = "Blue Box",
            titleRomaji = "Ao no Hako",
            titleJapanese = "アオのハコ",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1359/143714.jpg",
            largeImageUrl = "https://cdn.myanimelist.net/images/anime/1359/143714.jpg",
            synopsis = "Taiki Inomata treina badminton no colegial e se apaixona por Chinatsu Kano, estrela do basquete.",
            score = 8.3,
            episodes = 25,
            status = "RELEASING",
            aired = "2024-10-03",
            broadcast = BroadcastInfo(day = "Segunda", time = "23:56", timezone = "JST", stringFormat = "Segunda às 23:56"),
            airingDay = "Segunda",
            genres = listOf("Romance", "Esportes", "Slice of Life"),
            externalLinks = listOf(ExternalLinkInfo("Netflix", "https://www.netflix.com"))
        ),

        // ================= TERÇA =================
        Anime(
            id = 201,
            title = "Chainsaw Man",
            titleEnglish = "Chainsaw Man",
            titleRomaji = "Chainsaw Man",
            titleJapanese = "チェンソーマン",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1806/126216.jpg",
            largeImageUrl = "https://cdn.myanimelist.net/images/anime/1806/126216.jpg",
            synopsis = "Denji, um jovem caçador de demônios, une-se ao demônio motosserra Pochita e se torna o Chainsaw Man.",
            score = 8.6,
            episodes = 12,
            status = "RELEASING",
            aired = "2022-10-12",
            broadcast = BroadcastInfo(day = "Terça", time = "24:00", timezone = "JST", stringFormat = "Terça às 24:00"),
            airingDay = "Terça",
            genres = listOf("Ação", "Sobrenatural", "Terror"),
            externalLinks = listOf(
                ExternalLinkInfo("Crunchyroll", "https://www.crunchyroll.com"),
                ExternalLinkInfo("Prime Video", "https://www.primevideo.com")
            )
        ),
        Anime(
            id = 202,
            title = "Black Clover",
            titleEnglish = "Black Clover",
            titleRomaji = "Black Clover",
            titleJapanese = "ブラッククローバー",
            imageUrl = "https://cdn.myanimelist.net/images/anime/2/88336.jpg",
            largeImageUrl = "https://cdn.myanimelist.net/images/anime/2/88336.jpg",
            synopsis = "Asta e Yuno buscam se tornar o Rei Mago do Reino Clover através da determinação e magia.",
            score = 8.1,
            episodes = 170,
            status = "RELEASING",
            aired = "2017-10-03",
            broadcast = BroadcastInfo(day = "Terça", time = "18:25", timezone = "JST", stringFormat = "Terça às 18:25"),
            airingDay = "Terça",
            genres = listOf("Ação", "Comédia", "Fantasia"),
            externalLinks = listOf(
                ExternalLinkInfo("Crunchyroll", "https://www.crunchyroll.com"),
                ExternalLinkInfo("Netflix", "https://www.netflix.com")
            )
        ),
        Anime(
            id = 203,
            title = "That Time I Got Reincarnated as a Slime Season 3",
            titleEnglish = "That Time I Got Reincarnated as a Slime Season 3",
            titleRomaji = "Tensei shitara Slime Datta Ken 3rd Season",
            titleJapanese = "転生したらスライムだった件",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1179/141525.jpg",
            largeImageUrl = "https://cdn.myanimelist.net/images/anime/1179/141525.jpg",
            synopsis = "Rimuru Tempest abre negociações comerciais e políticas com o Sacro Império de Lubelius.",
            score = 8.4,
            episodes = 24,
            status = "RELEASING",
            aired = "2024-04-05",
            broadcast = BroadcastInfo(day = "Terça", time = "23:00", timezone = "JST", stringFormat = "Terça às 23:00"),
            airingDay = "Terça",
            genres = listOf("Ação", "Aventura", "Fantasia"),
            externalLinks = listOf(ExternalLinkInfo("Crunchyroll", "https://www.crunchyroll.com"))
        ),

        // ================= QUARTA =================
        Anime(
            id = 301,
            title = "Re:ZERO - Starting Life in Another World Season 3",
            titleEnglish = "Re:ZERO - Starting Life in Another World Season 3",
            titleRomaji = "Re:Zero kara Hajimeru Isekai Seikatsu 3rd Season",
            titleJapanese = "Re:ゼロから始める異世界生活",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1862/141029.jpg",
            largeImageUrl = "https://cdn.myanimelist.net/images/anime/1862/141029.jpg",
            synopsis = "Subaru Natsuki enfrenta a Seita do Culto da Bruxa em Priestella para salvar Emilia e seus aliados.",
            score = 8.7,
            episodes = 16,
            status = "RELEASING",
            aired = "2024-10-02",
            broadcast = BroadcastInfo(day = "Quarta", time = "22:30", timezone = "JST", stringFormat = "Quarta às 22:30"),
            airingDay = "Quarta",
            genres = listOf("Drama", "Fantasia", "Suspense"),
            externalLinks = listOf(ExternalLinkInfo("Crunchyroll", "https://www.crunchyroll.com"))
        ),
        Anime(
            id = 302,
            title = "Oshi no Ko Season 2",
            titleEnglish = "Oshi no Ko Season 2",
            titleRomaji = "Oshi no Ko 2nd Season",
            titleJapanese = "【推しの子】",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1089/143321.jpg",
            largeImageUrl = "https://cdn.myanimelist.net/images/anime/1089/143321.jpg",
            synopsis = "Aqua e Ruby mergulham nos bastidores do mundo do entretenimento no arco da peça de teatro de Tóquio Blade.",
            score = 8.5,
            episodes = 13,
            status = "RELEASING",
            aired = "2024-07-03",
            broadcast = BroadcastInfo(day = "Quarta", time = "23:00", timezone = "JST", stringFormat = "Quarta às 23:00"),
            airingDay = "Quarta",
            genres = listOf("Drama", "Mistério", "Sobrenatural"),
            externalLinks = listOf(ExternalLinkInfo("HIDIVE", "https://www.hidive.com"))
        ),
        Anime(
            id = 303,
            title = "Dr. STONE: Science Future",
            titleEnglish = "Dr. STONE: Science Future",
            titleRomaji = "Dr. Stone: Science Future",
            titleJapanese = "Dr.STONE SCIENCE FUTURE",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1410/144685.jpg",
            largeImageUrl = "https://cdn.myanimelist.net/images/anime/1410/144685.jpg",
            synopsis = "Senku e o Reino da Ciência iniciam a construção do foguete espacial para desvendar a petrificação mundial.",
            score = 8.8,
            episodes = 12,
            status = "RELEASING",
            aired = "2025-01-10",
            broadcast = BroadcastInfo(day = "Quarta", time = "22:00", timezone = "JST", stringFormat = "Quarta às 22:00"),
            airingDay = "Quarta",
            genres = listOf("Aventura", "Sci-Fi", "Comédia"),
            externalLinks = listOf(ExternalLinkInfo("Crunchyroll", "https://www.crunchyroll.com"))
        ),

        // ================= QUINTA =================
        Anime(
            id = 401,
            title = "Jujutsu Kaisen",
            titleEnglish = "Jujutsu Kaisen",
            titleRomaji = "Jujutsu Kaisen",
            titleJapanese = "呪術廻戦",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1171/109222.jpg",
            largeImageUrl = "https://cdn.myanimelist.net/images/anime/1171/109222.jpg",
            synopsis = "Yuji Itadori engole o dedo do Rei das Maldições Sukuna e entra para a Escola Técnica de Feitiçaria de Tóquio.",
            score = 8.8,
            episodes = 47,
            status = "RELEASING",
            aired = "2020-10-03",
            broadcast = BroadcastInfo(day = "Quinta", time = "23:56", timezone = "JST", stringFormat = "Quinta às 23:56"),
            airingDay = "Quinta",
            genres = listOf("Ação", "Fantasia", "Sobrenatural"),
            externalLinks = listOf(
                ExternalLinkInfo("Crunchyroll", "https://www.crunchyroll.com"),
                ExternalLinkInfo("Netflix", "https://www.netflix.com")
            )
        ),
        Anime(
            id = 402,
            title = "Dandadan",
            titleEnglish = "Dandadan",
            titleRomaji = "Dandadan",
            titleJapanese = "ダンダダン",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1645/144214.jpg",
            largeImageUrl = "https://cdn.myanimelist.net/images/anime/1645/144214.jpg",
            synopsis = "Momo Ayase e Okarun desafiam fantasmas e alienígenas em uma aventura caótica e cheia de ação.",
            score = 8.6,
            episodes = 12,
            status = "RELEASING",
            aired = "2024-10-04",
            broadcast = BroadcastInfo(day = "Quinta", time = "24:26", timezone = "JST", stringFormat = "Quinta às 24:26"),
            airingDay = "Quinta",
            genres = listOf("Ação", "Comédia", "Sobrenatural"),
            externalLinks = listOf(
                ExternalLinkInfo("Crunchyroll", "https://www.crunchyroll.com"),
                ExternalLinkInfo("Netflix", "https://www.netflix.com")
            )
        ),
        Anime(
            id = 403,
            title = "Dr. Stone",
            titleEnglish = "Dr. Stone",
            titleRomaji = "Dr. Stone",
            titleJapanese = "Dr.STONE",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1613/102874.jpg",
            largeImageUrl = "https://cdn.myanimelist.net/images/anime/1613/102874.jpg",
            synopsis = "Milhares de anos após a humanidade ser petrificada, o gênio Senku desperta e usa a ciência para reiniciar a civilização.",
            score = 8.3,
            episodes = 35,
            status = "RELEASING",
            aired = "2019-07-05",
            broadcast = BroadcastInfo(day = "Quinta", time = "22:00", timezone = "JST", stringFormat = "Quinta às 22:00"),
            airingDay = "Quinta",
            genres = listOf("Aventura", "Sci-Fi", "Shounen"),
            externalLinks = listOf(ExternalLinkInfo("Crunchyroll", "https://www.crunchyroll.com"))
        ),

        // ================= SEXTA =================
        Anime(
            id = 501,
            title = "Frieren: Beyond Journey's End",
            titleEnglish = "Frieren: Beyond Journey's End",
            titleRomaji = "Sousou no Frieren",
            titleJapanese = "葬送のフリーレン",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1015/138025.jpg",
            largeImageUrl = "https://cdn.myanimelist.net/images/anime/1015/138025.jpg",
            synopsis = "A elfa vaga Frieren viaja pelo continente para entender a humanidade e o significado do tempo após a morte do herói Himmel.",
            score = 9.3,
            episodes = 28,
            status = "RELEASING",
            aired = "2023-09-29",
            broadcast = BroadcastInfo(day = "Sexta", time = "23:00", timezone = "JST", stringFormat = "Sexta às 23:00"),
            airingDay = "Sexta",
            genres = listOf("Aventura", "Drama", "Fantasia"),
            externalLinks = listOf(
                ExternalLinkInfo("Crunchyroll", "https://www.crunchyroll.com"),
                ExternalLinkInfo("Netflix", "https://www.netflix.com")
            )
        ),
        Anime(
            id = 502,
            title = "Shangri-La Frontier Season 2",
            titleEnglish = "Shangri-La Frontier Season 2",
            titleRomaji = "Shangri-La Frontier 2nd Season",
            titleJapanese = "シャングリラ・フロンティア",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1410/144685.jpg",
            largeImageUrl = "https://cdn.myanimelist.net/images/anime/1410/144685.jpg",
            synopsis = "Sunraku continua explorando o mundo de VRMMO de Shangri-La Frontier derrotando monstros lendários.",
            score = 8.1,
            episodes = 25,
            status = "RELEASING",
            aired = "2024-10-13",
            broadcast = BroadcastInfo(day = "Sexta", time = "17:00", timezone = "JST", stringFormat = "Sexta às 17:00"),
            airingDay = "Sexta",
            genres = listOf("Ação", "Aventura", "Jogos"),
            externalLinks = listOf(ExternalLinkInfo("Crunchyroll", "https://www.crunchyroll.com"))
        ),
        Anime(
            id = 503,
            title = "Mashle: Magic and Muscles Season 2",
            titleEnglish = "Mashle: Magic and Muscles Season 2",
            titleRomaji = "Mashle: Shinkakusha Kouho Senbatsu Shiken-hen",
            titleJapanese = "マッシュル-MASHLE-",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1594/140277.jpg",
            largeImageUrl = "https://cdn.myanimelist.net/images/anime/1594/140277.jpg",
            synopsis = "Mash Burnedead usa pura força física em um mundo onde a magia é tudo para se tornar um Visionário Divino.",
            score = 8.0,
            episodes = 12,
            status = "RELEASING",
            aired = "2024-01-06",
            broadcast = BroadcastInfo(day = "Sexta", time = "23:30", timezone = "JST", stringFormat = "Sexta às 23:30"),
            airingDay = "Sexta",
            genres = listOf("Ação", "Comédia", "Fantasia"),
            externalLinks = listOf(ExternalLinkInfo("Crunchyroll", "https://www.crunchyroll.com"))
        ),

        // ================= SÁBADO =================
        Anime(
            id = 601,
            title = "Bleach: Thousand-Year Blood War",
            titleEnglish = "Bleach: Thousand-Year Blood War",
            titleRomaji = "Bleach: Sennen Kessen-hen",
            titleJapanese = "BLEACH 千年血戦篇",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1764/126627.jpg",
            largeImageUrl = "https://cdn.myanimelist.net/images/anime/1764/126627.jpg",
            synopsis = "O confronto final entre Ceifeiros de Almas e Quincies atinge o Palácio Real do Rei das Almas.",
            score = 9.0,
            episodes = 13,
            status = "RELEASING",
            aired = "2024-10-05",
            broadcast = BroadcastInfo(day = "Sábado", time = "23:00", timezone = "JST", stringFormat = "Sábado às 23:00"),
            airingDay = "Sábado",
            genres = listOf("Ação", "Fantasia", "Sobrenatural"),
            externalLinks = listOf(
                ExternalLinkInfo("Disney+", "https://www.disneyplus.com"),
                ExternalLinkInfo("Hulu", "https://www.hulu.com")
            )
        ),
        Anime(
            id = 602,
            title = "Blue Lock Season 2",
            titleEnglish = "Blue Lock Season 2",
            titleRomaji = "Blue Lock vs. U-20 Japan",
            titleJapanese = "ブルーロック",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1287/138988.jpg",
            largeImageUrl = "https://cdn.myanimelist.net/images/anime/1287/138988.jpg",
            synopsis = "O projeto Blue Lock enfrenta a Seleção Sub-20 do Japão em uma partida histórica decisiva.",
            score = 8.2,
            episodes = 14,
            status = "RELEASING",
            aired = "2024-10-05",
            broadcast = BroadcastInfo(day = "Sábado", time = "23:30", timezone = "JST", stringFormat = "Sábado às 23:30"),
            airingDay = "Sábado",
            genres = listOf("Esportes", "Ação", "Drama"),
            externalLinks = listOf(ExternalLinkInfo("Crunchyroll", "https://www.crunchyroll.com"))
        ),
        Anime(
            id = 603,
            title = "Kaiju No. 8",
            titleEnglish = "Kaiju No. 8",
            titleRomaji = "Kaiju 8-gou",
            titleJapanese = "怪獣8号",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1583/141029.jpg",
            largeImageUrl = "https://cdn.myanimelist.net/images/anime/1583/141029.jpg",
            synopsis = "Kafka Hibino ganha a habilidade de se transformar em um monstro e se une à Força de Defesa contra Kaijus.",
            score = 8.4,
            episodes = 12,
            status = "RELEASING",
            aired = "2024-04-13",
            broadcast = BroadcastInfo(day = "Sábado", time = "23:00", timezone = "JST", stringFormat = "Sábado às 23:00"),
            airingDay = "Sábado",
            genres = listOf("Ação", "Sci-Fi", "Sobrenatural"),
            externalLinks = listOf(
                ExternalLinkInfo("Crunchyroll", "https://www.crunchyroll.com"),
                ExternalLinkInfo("Netflix", "https://www.netflix.com")
            )
        ),

        // ================= DOMINGO =================
        Anime(
            id = 701,
            title = "One Piece",
            titleEnglish = "One Piece",
            titleRomaji = "One Piece",
            titleJapanese = "ONE PIECE",
            imageUrl = "https://cdn.myanimelist.net/images/anime/6/73245.jpg",
            largeImageUrl = "https://cdn.myanimelist.net/images/anime/6/73245.jpg",
            synopsis = "Luffy e os Chapéus de Palha navegam pelo Grand Line no lendário arco de Egghead.",
            score = 8.7,
            episodes = 1120,
            status = "RELEASING",
            aired = "1999-10-20",
            broadcast = BroadcastInfo(day = "Domingo", time = "09:30", timezone = "JST", stringFormat = "Domingo às 09:30"),
            airingDay = "Domingo",
            genres = listOf("Ação", "Aventura", "Fantasia"),
            externalLinks = listOf(
                ExternalLinkInfo("Crunchyroll", "https://www.crunchyroll.com"),
                ExternalLinkInfo("Netflix", "https://www.netflix.com")
            )
        ),
        Anime(
            id = 702,
            title = "Demon Slayer: Kimetsu no Yaiba - Hashira Training Arc",
            titleEnglish = "Demon Slayer: Kimetsu no Yaiba Hashira Training Arc",
            titleRomaji = "Kimetsu no Yaiba: Hashira Geiko-hen",
            titleJapanese = "鬼滅の刃 柱稽古編",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1208/140228.jpg",
            largeImageUrl = "https://cdn.myanimelist.net/images/anime/1208/140228.jpg",
            synopsis = "Tanjiro e os caçadores passam pelo treinamento intensivo com os Hashiras antes da batalha final no Castelo Infinito.",
            score = 8.5,
            episodes = 8,
            status = "RELEASING",
            aired = "2024-05-12",
            broadcast = BroadcastInfo(day = "Domingo", time = "23:15", timezone = "JST", stringFormat = "Domingo às 23:15"),
            airingDay = "Domingo",
            genres = listOf("Ação", "Sobrenatural", "Fantasia"),
            externalLinks = listOf(
                ExternalLinkInfo("Crunchyroll", "https://www.crunchyroll.com"),
                ExternalLinkInfo("Netflix", "https://www.netflix.com")
            )
        ),
        Anime(
            id = 703,
            title = "The Apothecary Diaries",
            titleEnglish = "The Apothecary Diaries",
            titleRomaji = "Kusuriya no Hitorigoto",
            titleJapanese = "薬屋のひとりごと",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1707/136066.jpg",
            largeImageUrl = "https://cdn.myanimelist.net/images/anime/1707/136066.jpg",
            synopsis = "Maomao usa seus vastos conhecimentos de farmacologia e medicina para solucionar mistérios no palácio imperial chinês.",
            score = 8.9,
            episodes = 24,
            status = "RELEASING",
            aired = "2023-10-22",
            broadcast = BroadcastInfo(day = "Domingo", time = "24:55", timezone = "JST", stringFormat = "Domingo às 24:55"),
            airingDay = "Domingo",
            genres = listOf("Drama", "Mistério", "Slice of Life"),
            externalLinks = listOf(ExternalLinkInfo("Crunchyroll", "https://www.crunchyroll.com"))
        )
    )
}
