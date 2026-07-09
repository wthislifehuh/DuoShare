package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String, // Encrypted prior to storage
    val amount: Double,
    val date: Long,
    val paidBy: String, // "Partner A", "Partner B", "Joint"
    val splitRule: String, // "EQUAL", "60_40", "70_30", "A_ONLY", "B_ONLY"
    val category: String, // "Housing", "Food", "Entertainment", "Utilities", "Transport", "Other"
    val isRecurring: Boolean = false,
    val recurrencePeriod: String? = null, // "WEEKLY", "MONTHLY"
    val isBankSynced: Boolean = false
)

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val targetAmount: Double,
    var currentAmount: Double,
    val targetDate: Long,
    val category: String
)

@Entity(tableName = "milestones")
data class Milestone(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val dueDate: Long,
    val completed: Boolean = false,
    val notes: String = "" // Encrypted prior to storage
)

@Entity(tableName = "bank_connections")
data class BankConnection(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val institutionName: String,
    val accountName: String,
    val balance: Double,
    val lastSynced: Long,
    val encryptedCredentials: String? = null
)
