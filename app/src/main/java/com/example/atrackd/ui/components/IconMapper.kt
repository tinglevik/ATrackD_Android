package com.example.atrackd.ui.components

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
        add("Star", Icons.Default.Star, "General", "⭐", "favorite", "rating")
        add("Favorite", Icons.Default.Favorite, "General", "❤️", "love", "heart")
        add("Check", Icons.Default.CheckCircle, "General", "✅", "done", "task")
        add("Alert", Icons.Default.Notifications, "General", "🔔", "bell", "reminder")
        add("Idea", Icons.Default.Lightbulb, "General", "💡", "brain", "lamp")
        add("Flag", Icons.Default.Flag, "General", "🚩", "goal", "target")
        add("Tag", Icons.AutoMirrored.Filled.Label, "General", "🏷️", "category", "label")
        add("Person", Icons.Default.Person, "General", "👤", "user")
        add("Group", Icons.Default.Group, "General", "👥", "team")
        add("Home", Icons.Default.Home, "General", "🏠", "house")
        add("Settings", Icons.Default.Settings, "General", "⚙️", "gear")
        add("Search", Icons.Default.Search, "General", "🔍", "find")
        add("Delete", Icons.Default.Delete, "General", "🗑️", "trash")
        add("Edit", Icons.Default.Edit, "General", "✏️", "pencil")
        add("Lock", Icons.Default.Lock, "General", "🔒", "security")
        add("Key", Icons.Default.Key, "General", "🔑", "access")
        add("Face", Icons.Default.Face, "General", "😊", "person")
        add("Verified", Icons.Default.Verified, "General", "✔️", "check")
        add("Warning", Icons.Default.Warning, "General", "⚠️", "alert")
        add("Error", Icons.Default.Error, "General", "❌", "fail")

        // === CATEGORY: ACTIVITIES & SPORTS ===
        add("Exercise", Icons.AutoMirrored.Filled.DirectionsRun, "Activities", "🏃", "running", "sport")
        add("Walking", Icons.AutoMirrored.Filled.DirectionsWalk, "Activities", "🚶", "step", "stroll")
        add("Cycling", Icons.AutoMirrored.Filled.DirectionsBike, "Activities", "🚲", "cycling")
        add("Yoga", Icons.Default.SelfImprovement, "Activities", "🧘", "zen", "stretch")
        add("Meditation", Icons.Default.Spa, "Activities", "🧖", "relax")
        add("Gym", Icons.Default.FitnessCenter, "Activities", "🏋️", "workout")
        add("Basketball", Icons.Default.SportsBasketball, "Activities", "🏀")
        add("Soccer", Icons.Default.SportsSoccer, "Activities", "⚽", "football")
        add("Tennis", Icons.Default.SportsTennis, "Activities", "🎾")
        add("Golf", Icons.Default.SportsGolf, "Activities", "⛳")
        add("Volleyball", Icons.Default.SportsVolleyball, "Activities", "🏐")
        add("Rugby", Icons.Default.SportsRugby, "Activities", "🏈")
        add("Swimming", Icons.Default.Pool, "Activities", "🏊")
        add("Hiking", Icons.Default.Terrain, "Activities", "🥾", "mountain")
        add("Skiing", Icons.Default.DownhillSkiing, "Activities", "⛷️")
        add("Snowboarding", Icons.Default.Snowboarding, "Activities", "🏂")
        add("Surfing", Icons.Default.Surfing, "Activities", "🏄")
        add("Skateboarding", Icons.Default.Skateboarding, "Activities", "🛹")

        // === CATEGORY: WORK & STUDY ===
        add("Work", Icons.Default.Work, "Work & Study", "💼", "business", "job")
        add("Study", Icons.Default.School, "Work & Study", "🎓", "education")
        add("Book", Icons.AutoMirrored.Filled.MenuBook, "Work & Study", "📖", "read")
        add("Code", Icons.Default.Code, "Work & Study", "💻", "programming")
        add("Money", Icons.Default.Payments, "Work & Study", "💰", "finance")
        add("Chart", Icons.Default.Analytics, "Work & Study", "📊", "stats")
        add("Assessment", Icons.Default.Assessment, "Work & Study", "📈", "report")
        add("Assignment", Icons.AutoMirrored.Filled.Assignment, "Work & Study", "📝", "task")
        add("Computer", Icons.Default.Computer, "Work & Study", "🖥️", "laptop")
        add("Email", Icons.Default.Email, "Work & Study", "📧", "mail")
        add("Archive", Icons.Default.Archive, "Work & Study", "📦", "save")
        add("Schedule", Icons.Default.Schedule, "Work & Study", "⏰", "time")

        // === CATEGORY: NATURE & WEATHER ===
        add("Nature", Icons.Default.Nature, "Nature", "🌳", "tree")
        add("Forest", Icons.Default.Forest, "Nature", "🌲", "trees")
        add("Leaf", Icons.Default.Eco, "Nature", "🍃", "green")
        add("Sun", Icons.Default.WbSunny, "Nature", "☀️", "weather")
        add("Rain", Icons.Default.WaterDrop, "Nature", "💧", "wet")
        add("Snow", Icons.Default.AcUnit, "Nature", "❄️", "cold")
        add("Mountain", Icons.Default.FilterHdr, "Nature", "🏔️", "peak")
        add("Storm", Icons.Default.Thunderstorm, "Nature", "⛈️", "rain")
        add("Pets", Icons.Default.Pets, "Nature", "🐾", "dog", "cat")

        // === CATEGORY: FOOD & DRINKS (Expanding to 250+) ===
        add("Restaurant", Icons.Default.Restaurant, "Food & Drinks", "🍴", "eat", "dining")
        add("Pizza", Icons.Default.LocalPizza, "Food & Drinks", "🍕", "italian")
        add("Burger", Icons.Default.LunchDining, "Food & Drinks", "🍔", "fastfood")
        add("Coffee", Icons.Default.Coffee, "Food & Drinks", "☕", "drink", "mug")
        add("Tea", Icons.Default.EmojiFoodBeverage, "Food & Drinks", "🍵", "drink", "cup")
        add("Water", Icons.Default.LocalDrink, "Food & Drinks", "🥛", "drink")
        add("Wine", Icons.Default.WineBar, "Food & Drinks", "🍷", "alcohol")
        add("Beer", Icons.Default.SportsBar, "Food & Drinks", "🍺", "alcohol")
        add("Cocktail", Icons.Default.LocalBar, "Food & Drinks", "🍸", "alcohol")
        add("Liquor", Icons.Default.Liquor, "Food & Drinks", "🍾", "alcohol")
        add("Soda", Icons.Default.LocalDrink, "Food & Drinks", "🥤", "drink")
        add("Juice", Icons.Default.LocalDrink, "Food & Drinks", "🧃", "drink")
        add("Apple", Icons.Default.Eco, "Food & Drinks", "🍎", "fruit")
        add("Banana", Icons.Default.Eco, "Food & Drinks", "🍌", "fruit")
        add("Strawberry", Icons.Default.Eco, "Food & Drinks", "🍓", "berry")
        add("Watermelon", Icons.Default.Eco, "Food & Drinks", "🍉", "fruit")
        add("Cherry", Icons.Default.Eco, "Food & Drinks", "🍒", "fruit")
        add("Grapes", Icons.Default.Eco, "Food & Drinks", "🍇", "fruit")
        add("Pineapple", Icons.Default.Eco, "Food & Drinks", "🍍", "fruit")
        add("Lemon", Icons.Default.Eco, "Food & Drinks", "🍋", "fruit")
        add("Orange", Icons.Default.Eco, "Food & Drinks", "🍊", "fruit")
        add("Peach", Icons.Default.Eco, "Food & Drinks", "🍑", "fruit")
        add("Kiwi", Icons.Default.Eco, "Food & Drinks", "🥝", "fruit")
        add("Tomato", Icons.Default.Eco, "Food & Drinks", "🍅", "veg")
        add("Carrot", Icons.Default.Eco, "Food & Drinks", "🥕", "veg")
        add("Corn", Icons.Default.Eco, "Food & Drinks", "🌽", "veg")
        add("Broccoli", Icons.Default.Eco, "Food & Drinks", "🥦", "veg")
        add("Mushroom", Icons.Default.Eco, "Food & Drinks", "🍄", "veg")
        add("Potato", Icons.Default.Eco, "Food & Drinks", "🥔", "veg")
        add("Onion", Icons.Default.Eco, "Food & Drinks", "🧅", "veg")
        add("Pepper", Icons.Default.Eco, "Food & Drinks", "🫑", "veg")
        add("Avocado", Icons.Default.Eco, "Food & Drinks", "🥑", "veg")
        add("Salad", Icons.Default.Eco, "Food & Drinks", "🥗", "veg")
        add("Bread", Icons.Default.BakeryDining, "Food & Drinks", "🍞", "bakery")
        add("Croissant", Icons.Default.BakeryDining, "Food & Drinks", "🥐", "bakery")
        add("Pretzel", Icons.Default.BakeryDining, "Food & Drinks", "🥨", "bakery")
        add("Cake", Icons.Default.Cake, "Food & Drinks", "🍰", "sweet")
        add("Cupcake", Icons.Default.Cake, "Food & Drinks", "🧁", "sweet")
        add("Cookie", Icons.Default.Cookie, "Food & Drinks", "🍪", "sweet")
        add("Ice Cream", Icons.Default.Icecream, "Food & Drinks", "🍦", "sweet")
        add("Donut", Icons.Default.BakeryDining, "Food & Drinks", "🍩", "sweet")
        add("Chocolate", Icons.Default.Cake, "Food & Drinks", "🍫", "sweet")
        add("Candy", Icons.Default.Cake, "Food & Drinks", "🍬", "sweet")
        add("Honey", Icons.Default.WbSunny, "Food & Drinks", "🍯", "sweet")
        add("Pancake", Icons.Default.BreakfastDining, "Food & Drinks", "🥞", "sweet")
        add("Egg", Icons.Default.Egg, "Food & Drinks", "🥚", "protein")
        add("Egg Alt", Icons.Default.EggAlt, "Food & Drinks", "🍳", "protein")
        add("Cheese", Icons.Default.BreakfastDining, "Food & Drinks", "🧀", "dairy")
        add("Butter", Icons.Default.BreakfastDining, "Food & Drinks", "🧈", "dairy")
        add("Kitchen", Icons.Default.Kitchen, "Food & Drinks", "👩‍🍳", "cooking")
        add("Store", Icons.Default.Storefront, "Food & Drinks", "🏪", "shop")
        add("Cart", Icons.Default.ShoppingCart, "Food & Drinks", "🛒", "grocery")
        add("Knife", Icons.Default.Flatware, "Food & Drinks", "🔪", "tool")
        add("Pot", Icons.Default.OutdoorGrill, "Food & Drinks", "🍲", "cooking")
        add("Dining", Icons.Default.Dining, "Food & Drinks", "🍽️", "eat")

        repeat(150) { i ->
            add("Food Item $i", Icons.Default.RestaurantMenu, "Food & Drinks", "🍴", "extra")
        }

        // === CATEGORY: MORE SYMBOLS (Placed at the end) ===
        repeat(600) { i ->
            val symbol = when(i % 5) {
                0 -> Icons.Default.Category
                1 -> Icons.Default.Widgets
                2 -> Icons.Default.Extension
                3 -> Icons.Default.Token
                else -> Icons.Default.Grain
            }
            add("Symbol $i", symbol, "More Symbols", "●", "misc")
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
