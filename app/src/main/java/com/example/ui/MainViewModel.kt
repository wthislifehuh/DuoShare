package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = FinanceRepository(database)

    // Shared UI state flows
    val expenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<SavingsGoal>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val milestones: StateFlow<List<Milestone>> = repository.allMilestones
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val connections: StateFlow<List<BankConnection>> = repository.allConnections
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val aiRecommendations = MutableStateFlow<String>("")
    val isAiLoading = MutableStateFlow(false)
    val budgetLimit = MutableStateFlow(2500.0)

    init {
        // Pre-populate with beautiful, realistic couple data if empty
        viewModelScope.launch(Dispatchers.IO) {
            expenses.first { true }.let { currentExpenses ->
                if (currentExpenses.isEmpty()) {
                    populateInitialDemoData()
                }
            }
        }
    }

    private suspend fun populateInitialDemoData() {
        val baseTime = System.currentTimeMillis()

        // Demo Expenses
        val initialExpenses = listOf(
            Expense(
                description = "Monthly Rent & Water",
                amount = 1200.00,
                date = baseTime - 604800000, // 7 days ago
                paidBy = "Partner A",
                splitRule = "60_40",
                category = "Housing",
                isRecurring = true,
                recurrencePeriod = "MONTHLY"
            ),
            Expense(
                description = "Organic Grocery Haul",
                amount = 142.50,
                date = baseTime - 172800000, // 2 days ago
                paidBy = "Partner B",
                splitRule = "EQUAL",
                category = "Food"
            ),
            Expense(
                description = "Electric Utility Bill",
                amount = 89.10,
                date = baseTime - 86400000, // 1 day ago
                paidBy = "Joint",
                splitRule = "EQUAL",
                category = "Utilities",
                isRecurring = true,
                recurrencePeriod = "MONTHLY"
            ),
            Expense(
                description = "Couples Weekend Movie Night",
                amount = 38.00,
                date = baseTime - 43200000, // 12 hours ago
                paidBy = "Partner B",
                splitRule = "EQUAL",
                category = "Entertainment"
            )
        )

        // Demo Savings Goals
        val initialGoals = listOf(
            SavingsGoal(
                name = "Paris Honeymoon Escapade",
                targetAmount = 5000.0,
                currentAmount = 1850.0,
                targetDate = baseTime + 15552000000L, // 180 days from now
                category = "Travel"
            ),
            SavingsGoal(
                name = "Emergency Nest Egg",
                targetAmount = 10000.0,
                currentAmount = 4500.0,
                targetDate = baseTime + 31536000000L, // 365 days from now
                category = "Savings"
            ),
            SavingsGoal(
                name = "Cozy Couch Upgrade",
                targetAmount = 1200.0,
                currentAmount = 800.0,
                targetDate = baseTime + 5184000000L, // 60 days from now
                category = "Furniture"
            )
        )

        // Demo Milestones
        val initialMilestones = listOf(
            Milestone(
                title = "Renew Apartment Lease",
                dueDate = baseTime + 2592000000L, // 30 days from now
                completed = false,
                notes = "Confirm landlord updates the kitchen countertop prior to signing."
            ),
            Milestone(
                title = "1st Anniversary Getaway",
                dueDate = baseTime + 10368000000L, // 120 days from now
                completed = false,
                notes = "Book hotel and schedule dinner in advance to get window seats."
            ),
            Milestone(
                title = "Create Shared Checking Account",
                dueDate = baseTime - 86400000 * 5, // 5 days ago
                completed = true,
                notes = "Linked online access and received debit cards!"
            )
        )

        // Demo Bank Connection
        val initialConnection = BankConnection(
            institutionName = "Chase Private Client",
            accountName = "Duo Ultimate Checking",
            balance = 4850.25,
            lastSynced = baseTime - 3600000,
            encryptedCredentials = "Linked Sandbox Account"
        )

        for (exp in initialExpenses) repository.insertExpense(exp)
        for (g in initialGoals) repository.insertGoal(g)
        for (m in initialMilestones) repository.insertMilestone(m)
        repository.insertConnection(initialConnection)
    }

    // Expense actions
    fun addExpense(
        description: String,
        amount: Double,
        paidBy: String,
        splitRule: String,
        category: String,
        isRecurring: Boolean,
        recurrencePeriod: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertExpense(
                Expense(
                    description = description,
                    amount = amount,
                    date = System.currentTimeMillis(),
                    paidBy = paidBy,
                    splitRule = splitRule,
                    category = category,
                    isRecurring = isRecurring,
                    recurrencePeriod = recurrencePeriod
                )
            )
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteExpense(expense)
        }
    }

    // Goals actions
    fun addGoal(name: String, targetAmount: Double, currentAmount: Double, targetDays: Int, category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val targetDate = System.currentTimeMillis() + (targetDays * 86400000L)
            repository.insertGoal(
                SavingsGoal(
                    name = name,
                    targetAmount = targetAmount,
                    currentAmount = currentAmount,
                    targetDate = targetDate,
                    category = category
                )
            )
        }
    }

    fun contributeToGoal(goal: SavingsGoal, amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = goal.copy(currentAmount = goal.currentAmount + amount)
            repository.insertGoal(updated)
        }
    }

    fun deleteGoal(goal: SavingsGoal) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteGoal(goal)
        }
    }

    // Milestone actions
    fun addMilestone(title: String, targetDays: Int, notes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val dueDate = System.currentTimeMillis() + (targetDays * 86400000L)
            repository.insertMilestone(
                Milestone(
                    title = title,
                    dueDate = dueDate,
                    completed = false,
                    notes = notes
                )
            )
        }
    }

    fun toggleMilestoneCompleted(milestone: Milestone) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = milestone.copy(completed = !milestone.completed)
            repository.insertMilestone(updated)
        }
    }

    fun deleteMilestone(milestone: Milestone) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMilestone(milestone)
        }
    }

    // Bank Actions
    fun linkBankConnection(institution: String, accountName: String, balance: Double, credentialsValue: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val connection = BankConnection(
                institutionName = institution,
                accountName = accountName,
                balance = balance,
                lastSynced = System.currentTimeMillis(),
                encryptedCredentials = credentialsValue
            )
            repository.insertConnection(connection)
            repository.syncBankTransactions(connection)
        }
    }

    fun syncBankConnection(connection: BankConnection) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.syncBankTransactions(connection)
        }
    }

    fun deleteBankConnection(connection: BankConnection) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteConnection(connection)
        }
    }

    // AI Savings Recommendations using Gemini 3.5 Flash
    fun generateAiSavingsRecommendations() {
        viewModelScope.launch(Dispatchers.IO) {
            isAiLoading.value = true
            aiRecommendations.value = ""

            val currentExpenses = expenses.value
            val currentGoals = goals.value

            val expensesText = if (currentExpenses.isEmpty()) {
                "No registered expenses."
            } else {
                currentExpenses.take(15).joinToString("\n") {
                    "- ${it.category}: $${it.amount} paid by ${it.paidBy} (${it.splitRule})"
                }
            }

            val goalsText = if (currentGoals.isEmpty()) {
                "No savings goals registered yet."
            } else {
                currentGoals.joinToString("\n") {
                    "- ${it.name}: $${it.currentAmount} saved out of $${it.targetAmount}"
                }
            }

            val prompt = """
                You are DuoShare AI, an expert couples' relationship & personal financial coach.
                Analyze this couple's financial state to generate personalized savings recommendations:
                - Monthly Budget Limit: $${budgetLimit.value}
                - Top Expenses:
                $expensesText
                - Goals progress:
                $goalsText

                Based on their categories, spending distributions, and joint targets, output:
                1. A warm, dual-partner encouraging greeting recognizing their team effort.
                2. Three concrete, actionable monthly savings opportunities (e.g. sharing utilities, smart grocery shopping, canceling inactive streaming services, adjusting temperature, or cooking dates instead of dining out). Show exactly how these savings speed up their goals.
                3. A personalized joint milestone recommendation that helps them bond financially.
                
                Keep the tone warm, affectionate, clear, and highly supportive. Format the text cleanly with bullet points and elegant spacing. Use simple markdown.
            """.trimIndent()

            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    // Fallback response if key is unconfigured, ensuring robust offline/prototype demonstration
                    delay(1500)
                    aiRecommendations.value = """
                        ### Welcome to DuoShare AI Assistant! ❤️
                        
                        It looks like your Gemini API Key is not fully active yet, but here are curated couples' recommendations based on your current setup:
                        
                        *   **Partner Coordination**: Rent is split **60/40** between Partner A and Partner B. Try shifting smaller, variable costs like groceries to a perfect **50/50** to simplify calculation overhead.
                        *   **Dining in Date Night**: You spent **$38.00** on movies and have dining expenses. Swap one meal out for a home-cooked "candlelit chef night". Average savings of **$65/month** will push your **Paris Honeymoon Escapade** 2 months closer!
                        *   **Utility Efficiency**: With **$89.10** on power bills, installing a smart thermostat could lower monthly utility spend by **15%**, saving **$13.40/month** effortlessly.
                        *   **Shared Milestones Tip**: You're doing outstanding with **$1,850** saved for Paris! Set a minor milestone to celebrate reaching the **$2,500** halfway mark with a cozy picnic.
                    """.trimIndent()
                } else {
                    val request = GeminiRequest(
                        contents = listOf(GeminiRequest.Content(parts = listOf(GeminiRequest.Part(prompt)))),
                        generationConfig = GeminiRequest.GenerationConfig(temperature = 0.7f)
                    )
                    val response = RetrofitGeminiClient.service.generateContent(apiKey, request)
                    val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    aiRecommendations.value = reply ?: "No recommendations could be generated. Check back shortly!"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                aiRecommendations.value = "Unable to reach DuoShare AI Coach. Please check your internet connection and try again."
            } finally {
                isAiLoading.value = false
            }
        }
    }

    // Helper delay for fallback simulation
    private suspend fun delay(time: Long) {
        withContext(Dispatchers.Default) {
            Thread.sleep(time)
        }
    }

    // Export Tax Report CSV via sharing sheet
    fun exportTaxReportCsv(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = expenses.value
            val csvBuilder = StringBuilder()
            csvBuilder.append("ID,Date,Description,Category,Amount,Paid By,Split Rule,Recurring,Synced\n")

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            for (exp in list) {
                val dateStr = dateFormat.format(Date(exp.date))
                // Clean description of commas to preserve CSV boundaries
                val cleanDesc = exp.description.replace(",", ";")
                csvBuilder.append("${exp.id},$dateStr,$cleanDesc,${exp.category},${exp.amount},${exp.paidBy},${exp.splitRule},${exp.isRecurring},${exp.isBankSynced}\n")
            }

            val csvContent = csvBuilder.toString()
            
            withContext(Dispatchers.Main) {
                try {
                    // Create local file in Cache to share securely
                    val file = File(context.cacheDir, "DuoShare_Tax_Report.csv")
                    file.writeText(csvContent, Charsets.UTF_8)

                    val authority = "${context.packageName}.fileprovider"
                    val uri = FileProvider.getUriForFile(context, authority, file)

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_SUBJECT, "DuoShare - Shared Expense Tax Report")
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    context.startActivity(Intent.createChooser(intent, "Export Shared Cost Tax Report"))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
