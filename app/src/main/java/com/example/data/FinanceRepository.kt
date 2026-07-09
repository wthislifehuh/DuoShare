package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

class FinanceRepository(private val database: AppDatabase) {
    private val expenseDao = database.expenseDao()
    private val savingsGoalDao = database.savingsGoalDao()
    private val milestoneDao = database.milestoneDao()
    private val bankConnectionDao = database.bankConnectionDao()

    // Decrypt descriptions reactively when loading
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses().map { list ->
        list.map { it.copy(description = SecurityManager.decrypt(it.description)) }
    }

    val recurringExpenses: Flow<List<Expense>> = expenseDao.getRecurringExpenses().map { list ->
        list.map { it.copy(description = SecurityManager.decrypt(it.description)) }
    }

    // Decrypt notes reactively
    val allMilestones: Flow<List<Milestone>> = milestoneDao.getAllMilestones().map { list ->
        list.map { it.copy(notes = SecurityManager.decrypt(it.notes)) }
    }

    val allGoals: Flow<List<SavingsGoal>> = savingsGoalDao.getAllGoals()

    val allConnections: Flow<List<BankConnection>> = bankConnectionDao.getAllConnections()

    // Encrypt descriptions prior to writing
    suspend fun insertExpense(expense: Expense) {
        val encrypted = expense.copy(description = SecurityManager.encrypt(expense.description))
        expenseDao.insertExpense(encrypted)
    }

    suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun deleteExpenseById(id: Int) {
        expenseDao.deleteExpenseById(id)
    }

    suspend fun insertGoal(goal: SavingsGoal) {
        savingsGoalDao.insertGoal(goal)
    }

    suspend fun deleteGoal(goal: SavingsGoal) {
        savingsGoalDao.deleteGoal(goal)
    }

    suspend fun insertMilestone(milestone: Milestone) {
        val encrypted = milestone.copy(notes = SecurityManager.encrypt(milestone.notes))
        milestoneDao.insertMilestone(encrypted)
    }

    suspend fun deleteMilestone(milestone: Milestone) {
        milestoneDao.deleteMilestone(milestone)
    }

    suspend fun insertConnection(connection: BankConnection) {
        val encrypted = connection.copy(
            encryptedCredentials = connection.encryptedCredentials?.let { SecurityManager.encrypt(it) }
        )
        bankConnectionDao.insertConnection(encrypted)
    }

    suspend fun deleteConnection(connection: BankConnection) {
        bankConnectionDao.deleteConnection(connection)
    }

    // Trigger bank account syncing, fetching transactions and auto-categorizing them
    suspend fun syncBankTransactions(connection: BankConnection) {
        // Update connection timestamp
        val updatedConnection = connection.copy(lastSynced = System.currentTimeMillis())
        insertConnection(updatedConnection)

        // Mock synchronized transactions from bank to demonstrate secure parsing and categorization
        val baseTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        val demoTransactions = listOf(
            Expense(
                description = "SafeWay Groceries (Synced)",
                amount = 78.45,
                date = baseTime - 120000000, // recent
                paidBy = "Joint",
                splitRule = "EQUAL",
                category = "Food",
                isBankSynced = true
            ),
            Expense(
                description = "Netflix Premium (Synced)",
                amount = 22.99,
                date = baseTime - 450000000,
                paidBy = "Partner A",
                splitRule = "EQUAL",
                category = "Entertainment",
                isRecurring = true,
                recurrencePeriod = "MONTHLY",
                isBankSynced = true
            ),
            Expense(
                description = "City Power & Light (Synced)",
                amount = 112.50,
                date = baseTime - 900000000,
                paidBy = "Partner B",
                splitRule = "60_40", // Custom split
                category = "Utilities",
                isRecurring = true,
                recurrencePeriod = "MONTHLY",
                isBankSynced = true
            ),
            Expense(
                description = "Couples Dining Bistro (Synced)",
                amount = 95.00,
                date = baseTime - 1800000000,
                paidBy = "Joint",
                splitRule = "EQUAL",
                category = "Food",
                isBankSynced = true
            ),
            Expense(
                description = "Shell Gas Station (Synced)",
                amount = 45.00,
                date = baseTime - 2500000000,
                paidBy = "Partner B",
                splitRule = "EQUAL",
                category = "Transport",
                isBankSynced = true
            )
        )

        // Add them to database with encryption handled automatically!
        for (tx in demoTransactions) {
            insertExpense(tx)
        }
    }
}
