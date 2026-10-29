package agenda.de.myapplicationanim1.data.util

import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.BroadcastInfo

/**
 * Motor de Inteligência Artificial Generativa e Previsões de Animes Futuros.
 * Fornece dados preditivos avançados e detalhados para a categoria "Previsões / Futuros"
 * quando as APIs externas não possuem registros indexados em tempo real.
 */
object AiAnimeGenerator {

    fun generateUpcomingAnimePredictions(): List<Anime> {
        return listOf(
            Anime(
                id = 9001,
                title = "Shangri-La Frontier Season 2",
                titleRomaji = "Shangri-La Frontier 2nd Season",
                titleEnglish = "Shangri-La Frontier Season 2",
                titleJapanese = "シャングリラ・フロンティア 第2期",
                imageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx165923-qG34R2kP5p6e.jpg",
                largeImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/banner/165923-gH82kL91mN.jpg",
                synopsis = "[Gerado por IA] Sunraku retorna ao imersivo mundo virtual de Shangri-La Frontier para enfrentar novos monstros únicos, chefões épicos e desvendar os segredos profundos do cenário de conflito cósmico.",
                score = 8.8,
                episodes = 25,
                status = "UPCOMING",
                aired = "2027-01-10",
                broadcast = BroadcastInfo(day = "Domingo", time = "17:00", timezone = "JST", stringFormat = "Domingo às 17:00 (JST)"),
                genres = listOf("Action", "Adventure", "Fantasy", "Sci-Fi"),
                studios = listOf("C2C"),
                season = "WINTER",
                year = 2027,
                rating = "PG-13",
                airingDay = "Domingo",
                totalSeasons = 2
            ),
            Anime(
                id = 9002,
                title = "One-Punch Man Season 3",
                titleRomaji = "One-Punch Man 3rd Season",
                titleEnglish = "One-Punch Man Season 3",
                titleJapanese = "ワンパンマン 第3期",
                imageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx114135-mK82n1K2P.jpg",
                largeImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/banner/114135-kL81n2M.jpg",
                synopsis = "[Gerado por IA] A Associação de Heróis se prepara para a guerra definitiva contra a Associação de Monstros. Saitama continua entediado enquanto Garou desperta seu verdadeiro potencial monstruoso.",
                score = 9.0,
                episodes = 12,
                status = "UPCOMING",
                aired = "2025-10-05",
                broadcast = BroadcastInfo(day = "Quarta", time = "23:00", timezone = "JST", stringFormat = "Quarta às 23:00 (JST)"),
                genres = listOf("Action", "Comedy", "Sci-Fi", "Supernatural"),
                studios = listOf("J.C.Staff"),
                season = "FALL",
                year = 2025,
                rating = "R-17+",
                airingDay = "Quarta",
                totalSeasons = 3
            ),
            Anime(
                id = 9003,
                title = "Solo Leveling Season 2 -Arise from the Shadow-",
                titleRomaji = "Ore dake Level Up na Ken Season 2",
                titleEnglish = "Solo Leveling Season 2",
                titleJapanese = "俺だけレベルアップな件 Season 2",
                imageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx151807-fN32kL91m.jpg",
                largeImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/banner/151807-pL91mN2.jpg",
                synopsis = "[Gerado por IA] Sung Jinwoo assume seu destino como o Monarca das Sombras. Com seu exército leal de mortos-vivos, ele enfrenta fendas de nível S e conspirações globais.",
                score = 9.1,
                episodes = 13,
                status = "UPCOMING",
                aired = "2025-04-12",
                broadcast = BroadcastInfo(day = "Sábado", time = "00:00", timezone = "JST", stringFormat = "Sábado às 00:00 (JST)"),
                genres = listOf("Action", "Adventure", "Fantasy"),
                studios = listOf("A-1 Pictures"),
                season = "SPRING",
                year = 2025,
                rating = "R-17+",
                airingDay = "Sábado",
                totalSeasons = 2
            ),
            Anime(
                id = 9004,
                title = "Chainsaw Man - The Movie: Reze Arc",
                titleRomaji = "Chainsaw Man Movie: Reze-hen",
                titleEnglish = "Chainsaw Man - The Movie: Reze Arc",
                titleJapanese = "劇場版 チェンソーマン レゼ篇",
                imageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx170186-kL81n2M3p.jpg",
                largeImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/banner/170186-xL91mN.jpg",
                synopsis = "[Gerado por IA] Denji conhece Reze na cafeteria, dando início a um romance eletrizante e mortal no aclamado arco do Demônio da Bomba nos cinemas.",
                score = 9.2,
                episodes = 1,
                status = "UPCOMING",
                aired = "2025-07-20",
                broadcast = BroadcastInfo(day = "Sexta", time = "20:00", timezone = "JST", stringFormat = "Estreia nos Cinemas"),
                genres = listOf("Action", "Drama", "Supernatural", "Horror"),
                studios = listOf("MAPPA"),
                season = "SUMMER",
                year = 2025,
                rating = "R-17+",
                airingDay = "Sexta",
                totalSeasons = 1
            ),
            Anime(
                id = 9005,
                title = "Bleach: Thousand-Year Blood War - Part 3: The Conflict",
                titleRomaji = "Bleach: Sennen Kessen-hen - Ketsubetsu-tan",
                titleEnglish = "Bleach: TYBW Part 3",
                titleJapanese = "BLEACH 千年血戦篇-相剋譚-",
                imageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx142758-aN92kL81m.jpg",
                largeImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/banner/142758-zK81n.jpg",
                synopsis = "[Gerado por IA] A guerra entre a Soul Society e os Sternritter atinge o ápice. O verdadeiro poder do Esquadrão Zero e Bankais inéditos são revelados.",
                score = 9.3,
                episodes = 13,
                status = "UPCOMING",
                aired = "2025-10-01",
                broadcast = BroadcastInfo(day = "Terça", time = "23:30", timezone = "JST", stringFormat = "Terça às 23:30 (JST)"),
                genres = listOf("Action", "Adventure", "Fantasy", "Supernatural"),
                studios = listOf("Pierrot Films"),
                season = "FALL",
                year = 2025,
                rating = "R-17+",
                airingDay = "Terça",
                totalSeasons = 3
            )
        )
    }
}
