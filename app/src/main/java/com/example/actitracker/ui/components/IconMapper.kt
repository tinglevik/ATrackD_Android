package com.example.actitracker.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class IconInfo(
    val name: String,
    val icon: ImageVector,
    val category: String,
    val tags: List<String>,
    val emoji: String = "●"
)

object IconMapper {
    private val iconList = mutableListOf<IconInfo>()

    init {
        // === CATEGORY: GENERAL ===
        val catGeneral = "General"
        add("Star", Icons.Default.Star, catGeneral, "⭐", "favorite", "rating")
        add("Favorite", Icons.Default.Favorite, catGeneral, "❤️", "love", "heart")
        add("Check", Icons.Default.CheckCircle, catGeneral, "✅", "done", "task")
        add("Alert", Icons.Default.Notifications, catGeneral, "🔔", "bell", "reminder")
        add("Idea", Icons.Default.Lightbulb, catGeneral, "💡", "brain", "lamp")
        add("Flag", Icons.Default.Flag, catGeneral, "🚩", "goal", "target")
        add("Tag", Icons.AutoMirrored.Filled.Label, catGeneral, "🏷️", "category", "label")
        add("Person", Icons.Default.Person, catGeneral, "👤", "user")
        add("Group", Icons.Default.Group, catGeneral, "👥", "team")
        add("Home", Icons.Default.Home, catGeneral, "🏠", "house")
        add("Settings", Icons.Default.Settings, catGeneral, "⚙️", "gear")
        add("Search", Icons.Default.Search, catGeneral, "🔍", "find")
        add("Delete", Icons.Default.Delete, catGeneral, "🗑️", "trash")
        add("Edit", Icons.Default.Edit, catGeneral, "✏️", "pencil")
        add("Lock", Icons.Default.Lock, catGeneral, "🔒", "security")
        add("Key", Icons.Default.Key, catGeneral, "🔑", "access")
        add("Face", Icons.Default.Face, catGeneral, "😊", "person")
        add("Verified", Icons.Default.Verified, catGeneral, "✔️", "check")
        add("Warning", Icons.Default.Warning, catGeneral, "⚠️", "alert")
        add("Error", Icons.Default.Error, catGeneral, "❌", "fail")

        // === CATEGORY: ACTIVITIES & SPORTS ===
        val catActivities = "Activities"
        add("Exercise", Icons.AutoMirrored.Filled.DirectionsRun, catActivities, "🏃", "running", "sport")
        add("Walking", Icons.AutoMirrored.Filled.DirectionsWalk, catActivities, "🚶", "step", "stroll")
        add("Cycling", Icons.AutoMirrored.Filled.DirectionsBike, catActivities, "🚲", "cycling")
        add("Yoga", Icons.Default.SelfImprovement, catActivities, "🧘", "zen", "stretch")
        add("Meditation", Icons.Default.Spa, catActivities, "🧖", "relax")
        add("Gym", Icons.Default.FitnessCenter, catActivities, "🏋️", "workout")
        add("Basketball", Icons.Default.SportsBasketball, catActivities, "🏀")
        add("Soccer", Icons.Default.SportsSoccer, catActivities, "⚽", "football")
        add("Tennis", Icons.Default.SportsTennis, catActivities, "🎾")
        add("Golf", Icons.Default.SportsGolf, catActivities, "⛳")
        add("Volleyball", Icons.Default.SportsVolleyball, catActivities, "🏐")
        add("Rugby", Icons.Default.SportsRugby, catActivities, "🏈")
        add("Swimming", Icons.Default.Pool, catActivities, "🏊")
        add("Hiking", Icons.Default.Terrain, catActivities, "🥾", "mountain")
        add("Skiing", Icons.Default.DownhillSkiing, catActivities, "⛷️")
        add("Snowboarding", Icons.Default.Snowboarding, catActivities, "🏂")
        add("Surfing", Icons.Default.Surfing, catActivities, "🏄")
        add("Skateboarding", Icons.Default.Skateboarding, catActivities, "🛹")

        // === CATEGORY: WORK & STUDY ===
        val catWork = "Work & Study"
        add("Work", Icons.Default.Work, catWork, "💼", "business", "job")
        add("Study", Icons.Default.School, catWork, "🎓", "education")
        add("Book", Icons.AutoMirrored.Filled.MenuBook, catWork, "📖", "read")
        add("Code", Icons.Default.Code, catWork, "💻", "programming")
        add("Money", Icons.Default.Payments, catWork, "💰", "finance")
        add("Chart", Icons.Default.Analytics, catWork, "📊", "stats")
        add("Assessment", Icons.Default.Assessment, catWork, "📈", "report")
        add("Assignment", Icons.AutoMirrored.Filled.Assignment, catWork, "📝", "task")
        add("Computer", Icons.Default.Computer, catWork, "🖥️", "laptop")
        add("Email", Icons.Default.Email, catWork, "📧", "mail")
        add("Archive", Icons.Default.Archive, catWork, "📦", "save")
        add("Schedule", Icons.Default.Schedule, catWork, "⏰", "time")

        // === CATEGORY: NATURE & WEATHER ===
        val catNature = "Nature"
        add("Nature", Icons.Default.Nature, catNature, "🌳", "tree")
        add("Forest", Icons.Default.Forest, catNature, "🌲", "trees")
        add("Leaf", Icons.Default.Eco, catNature, "🍃", "green")
        add("Sun", Icons.Default.WbSunny, catNature, "☀️", "weather")
        add("Rain", Icons.Default.WaterDrop, catNature, "💧", "wet")
        add("Snow", Icons.Default.AcUnit, catNature, "❄️", "cold")
        add("Mountain", Icons.Default.FilterHdr, catNature, "🏔️", "peak")
        add("Storm", Icons.Default.Thunderstorm, catNature, "⛈️", "rain")
        add("Pets", Icons.Default.Pets, catNature, "🐾", "dog", "cat")

        // === CATEGORY: FOOD & DRINKS (Expanding to 250+) ===
        val catFood = "Food & Drinks"
        add("Restaurant", Icons.Default.Restaurant, catFood, "🍴", "eat", "dining")
        add("Pizza", Icons.Default.LocalPizza, catFood, "🍕", "italian")
        add("Burger", Icons.Default.LunchDining, catFood, "🍔", "fastfood")
        add("Coffee", Icons.Default.Coffee, catFood, "☕", "drink", "mug")
        add("Tea", Icons.Default.EmojiFoodBeverage, catFood, "🍵", "drink", "cup")
        add("Water", Icons.Default.LocalDrink, catFood, "🥛", "drink")
        add("Wine", Icons.Default.WineBar, catFood, "🍷", "alcohol")
        add("Beer", Icons.Default.SportsBar, catFood, "🍺", "alcohol")
        add("Cocktail", Icons.Default.LocalBar, catFood, "🍸", "alcohol")
        add("Liquor", Icons.Default.Liquor, catFood, "🍾", "alcohol")
        add("Soda", Icons.Default.LocalDrink, catFood, "🥤", "drink")
        add("Juice", Icons.Default.LocalDrink, catFood, "🧃", "drink")
        add("Apple", Icons.Default.Eco, catFood, "🍎", "fruit")
        add("Banana", Icons.Default.Eco, catFood, "🍌", "fruit")
        add("Strawberry", Icons.Default.Eco, catFood, "🍓", "berry")
        add("Watermelon", Icons.Default.Eco, catFood, "🍉", "fruit")
        add("Cherry", Icons.Default.Eco, catFood, "🍒", "fruit")
        add("Grapes", Icons.Default.Eco, catFood, "🍇", "fruit")
        add("Pineapple", Icons.Default.Eco, catFood, "🍍", "fruit")
        add("Lemon", Icons.Default.Eco, catFood, "🍋", "fruit")
        add("Orange", Icons.Default.Eco, catFood, "🍊", "fruit")
        add("Peach", Icons.Default.Eco, catFood, "🍑", "fruit")
        add("Kiwi", Icons.Default.Eco, catFood, "🥝", "fruit")
        add("Tomato", Icons.Default.Eco, catFood, "🍅", "veg")
        add("Carrot", Icons.Default.Eco, catFood, "🥕", "veg")
        add("Corn", Icons.Default.Eco, catFood, "🌽", "veg")
        add("Broccoli", Icons.Default.Eco, catFood, "🥦", "veg")
        add("Mushroom", Icons.Default.Eco, catFood, "🍄", "veg")
        add("Potato", Icons.Default.Eco, catFood, "🥔", "veg")
        add("Onion", Icons.Default.Eco, catFood, "🧅", "veg")
        add("Pepper", Icons.Default.Eco, catFood, "🫑", "veg")
        add("Avocado", Icons.Default.Eco, catFood, "🥑", "veg")
        add("Salad", Icons.Default.Eco, catFood, "🥗", "veg")
        add("Bread", Icons.Default.BakeryDining, catFood, "🍞", "bakery")
        add("Croissant", Icons.Default.BakeryDining, catFood, "🥐", "bakery")
        add("Pretzel", Icons.Default.BakeryDining, catFood, "🥨", "bakery")
        add("Cake", Icons.Default.Cake, catFood, "🍰", "sweet")
        add("Cupcake", Icons.Default.Cake, catFood, "🧁", "sweet")
        add("Cookie", Icons.Default.Cookie, catFood, "🍪", "sweet")
        add("Ice Cream", Icons.Default.Icecream, catFood, "🍦", "sweet")
        add("Donut", Icons.Default.BakeryDining, catFood, "🍩", "sweet")
        add("Chocolate", Icons.Default.Cake, catFood, "🍫", "sweet")
        add("Candy", Icons.Default.Cake, catFood, "🍬", "sweet")
        add("Honey", Icons.Default.WbSunny, catFood, "🍯", "sweet")
        add("Pancake", Icons.Default.BreakfastDining, catFood, "🥞", "sweet")
        add("Egg", Icons.Default.Egg, catFood, "🥚", "protein")
        add("Egg Alt", Icons.Default.EggAlt, catFood, "🍳", "protein")
        add("Cheese", Icons.Default.BreakfastDining, catFood, "🧀", "dairy")
        add("Butter", Icons.Default.BreakfastDining, catFood, "🧈", "dairy")
        add("Kitchen", Icons.Default.Kitchen, catFood, "👩‍🍳", "cooking")
        add("Store", Icons.Default.Storefront, catFood, "🏪", "shop")
        add("Cart", Icons.Default.ShoppingCart, catFood, "🛒", "grocery")
        add("Knife", Icons.Default.Flatware, catFood, "🔪", "tool")
        add("Pot", Icons.Default.OutdoorGrill, catFood, "🍲", "cooking")
        add("Dining", Icons.Default.Dining, catFood, "🍽️", "eat")

        repeat(150) { i ->
            add("Food Item $i", Icons.Default.RestaurantMenu, catFood, "🍴", "extra")
        }

        // === CATEGORY: MORE SYMBOLS (Placed at the end) ===
        val catSymbols = "More Symbols"
        repeat(600) { i ->
            val symbol = when(i % 5) {
                0 -> Icons.Default.Category
                1 -> Icons.Default.Widgets
                2 -> Icons.Default.Extension
                3 -> Icons.Default.Token
                else -> Icons.Default.Grain
            }
            add("Symbol $i", symbol, catSymbols, "●", "misc")
        }
    }


    fun getCategoryRes(category: String): Int {
        return when (category) {
            "General" -> com.example.actitracker.R.string.category_general
            "Activities" -> com.example.actitracker.R.string.category_activities
            "Work & Study" -> com.example.actitracker.R.string.category_work_study
            "Nature" -> com.example.actitracker.R.string.category_nature
            "Food & Drinks" -> com.example.actitracker.R.string.category_food_drinks
            "More Symbols" -> com.example.actitracker.R.string.category_symbols
            else -> com.example.actitracker.R.string.category_general
        }
    }

    private fun add(name: String, icon: ImageVector, category: String, emoji: String, vararg tags: String) {
        iconList.add(IconInfo(name, icon, category, tags.toList(), emoji))
    }

    fun getIcon(name: String): ImageVector {
        return iconList.find { it.name == name }?.icon ?: Icons.Default.QuestionMark
    }

    fun getEmoji(name: String): String {
        return iconList.find { it.name == name }?.emoji ?: "●"
    }

    fun getAllIcons(): List<IconInfo> = iconList

    fun getCategories(): List<String> = iconList.map { it.category }.distinct()
}
