package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import kotlinx.coroutines.flow.StateFlow
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }

    // Collect Room DB states reactively
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val milestones by viewModel.milestones.collectAsStateWithLifecycle()
    val connections by viewModel.connections.collectAsStateWithLifecycle()

    val aiRecommendations by viewModel.aiRecommendations.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val budgetLimit by viewModel.budgetLimit.collectAsStateWithLifecycle()

    // Dialog trigger states
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddMilestoneDialog by remember { mutableStateOf(false) }
    var showLinkBankDialog by remember { mutableStateOf(false) }
    var showSettleUpDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "JOINT ACCOUNT",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Heart Icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DuoShare",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.exportTaxReportCsv(context)
                                Toast.makeText(context, "Preparing tax report CSV...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("export_tax_topbar_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Quick Share Tax CSV",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "S+J",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Expenses Tab") },
                    label = { Text("Expenses", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.PieChart, contentDescription = "Insights Tab") },
                    label = { Text("Insights", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.FavoriteBorder, contentDescription = "Couples Goals Tab") },
                    label = { Text("Goals", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Shield, contentDescription = "Bank & Security Tab") },
                    label = { Text("Security", fontSize = 11.sp) }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                ExtendedFloatingActionButton(
                    text = { Text("Add Expense", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add Expense Icon") },
                    onClick = { showAddExpenseDialog = true },
                    modifier = Modifier.testTag("add_expense_fab"),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            } else if (selectedTab == 2) {
                ExtendedFloatingActionButton(
                    text = { Text("New Goal") },
                    icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Add Goal Icon") },
                    onClick = { showAddGoalDialog = true },
                    modifier = Modifier.testTag("add_goal_fab"),
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = Color.White
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> ExpensesTab(
                    expenses = expenses,
                    onDeleteExpense = { viewModel.deleteExpense(it) },
                    onSettleUpClick = { showSettleUpDialog = true },
                    onTriggerReminder = { title ->
                        Toast.makeText(context, "📅 Payment reminder scheduled: $title", Toast.LENGTH_LONG).show()
                    }
                )
                1 -> InsightsTab(
                    expenses = expenses,
                    goals = goals,
                    budgetLimit = budgetLimit,
                    aiRecommendations = aiRecommendations,
                    isAiLoading = isAiLoading,
                    onGenerateAi = { viewModel.generateAiSavingsRecommendations() },
                    onExportReport = { viewModel.exportTaxReportCsv(context) },
                    onUpdateBudget = { viewModel.budgetLimit.value = it }
                )
                2 -> CouplesGoalsTab(
                    goals = goals,
                    milestones = milestones,
                    onContribute = { goal, amount -> viewModel.contributeToGoal(goal, amount) },
                    onDeleteGoal = { viewModel.deleteGoal(it) },
                    onToggleMilestone = { viewModel.toggleMilestoneCompleted(it) },
                    onDeleteMilestone = { viewModel.deleteMilestone(it) },
                    onAddMilestoneClick = { showAddMilestoneDialog = true }
                )
                3 -> BankSecurityTab(
                    connections = connections,
                    onLinkBankClick = { showLinkBankDialog = true },
                    onSyncBank = { viewModel.syncBankConnection(it) },
                    onDeleteBank = { viewModel.deleteBankConnection(it) }
                )
            }
        }
    }

    // --- Dialogs ---

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { showAddExpenseDialog = false },
            onConfirm = { desc, amount, paidBy, splitRule, category, isRecur, period ->
                viewModel.addExpense(desc, amount, paidBy, splitRule, category, isRecur, period)
                showAddExpenseDialog = false
            }
        )
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { name, target, current, days, category ->
                viewModel.addGoal(name, target, current, days, category)
                showAddGoalDialog = false
            }
        )
    }

    if (showAddMilestoneDialog) {
        AddMilestoneDialog(
            onDismiss = { showAddMilestoneDialog = false },
            onConfirm = { title, days, notes ->
                viewModel.addMilestone(title, days, notes)
                showAddMilestoneDialog = false
            }
        )
    }

    if (showLinkBankDialog) {
        LinkBankDialog(
            onDismiss = { showLinkBankDialog = false },
            onConfirm = { institution, account, balance, creds ->
                viewModel.linkBankConnection(institution, account, balance, creds)
                showLinkBankDialog = false
                Toast.makeText(context, "Connected securely to $institution! Syncing...", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showSettleUpDialog) {
        SettleUpDialog(
            expenses = expenses,
            onDismiss = { showSettleUpDialog = false },
            onConfirm = { payee, amount ->
                // To settle up, we record an opposite transaction balancing the account!
                viewModel.addExpense(
                    description = "Settle Balance: Paid $payee",
                    amount = amount,
                    paidBy = if (payee == "Partner A") "Partner B" else "Partner A",
                    splitRule = if (payee == "Partner A") "A_ONLY" else "B_ONLY", // 100% credited to other
                    category = "Settle Up",
                    isRecurring = false,
                    recurrencePeriod = null
                )
                showSettleUpDialog = false
                Toast.makeText(context, "🎉 Balance settled!", Toast.LENGTH_LONG).show()
            }
        )
    }
}

// ==================== EXPENSES TAB ====================

@Composable
fun ExpensesTab(
    expenses: List<Expense>,
    onDeleteExpense: (Expense) -> Unit,
    onSettleUpClick: () -> Unit,
    onTriggerReminder: (String) -> Unit
) {
    // Calculate net split balance
    // A paid X, B paid Y
    // For EQUAL splits: A owes B half, B owes A half
    // For 60_40: Partner A pays 60%, Partner B pays 40%.
    var totalPartnerA_Paid = 0.0
    var totalPartnerB_Paid = 0.0
    
    var partnerAOwesPartnerB = 0.0
    var partnerBOwesPartnerA = 0.0

    for (exp in expenses) {
        if (exp.category == "Settle Up") continue // Ignore settlement records in baseline math
        
        val amount = exp.amount
        when (exp.splitRule) {
            "EQUAL" -> {
                if (exp.paidBy == "Partner A") {
                    partnerBOwesPartnerA += amount / 2.0
                } else if (exp.paidBy == "Partner B") {
                    partnerAOwesPartnerB += amount / 2.0
                }
            }
            "60_40" -> {
                // Partner A pays 60%, Partner B pays 40%
                if (exp.paidBy == "Partner A") {
                    // A paid 100%. B owes 40% of amount.
                    partnerBOwesPartnerA += amount * 0.4
                } else if (exp.paidBy == "Partner B") {
                    // B paid 100%. A owes 60% of amount.
                    partnerAOwesPartnerB += amount * 0.6
                } else if (exp.paidBy == "Joint") {
                    // Joint paid 100%. But target is A:60%, B:40%. Joint has standard split, no immediate debit unless custom account.
                }
            }
            "70_30" -> {
                if (exp.paidBy == "Partner A") {
                    partnerBOwesPartnerA += amount * 0.3
                } else if (exp.paidBy == "Partner B") {
                    partnerAOwesPartnerB += amount * 0.7
                }
            }
            "A_ONLY" -> {
                if (exp.paidBy == "Partner B") {
                    partnerAOwesPartnerB += amount
                }
            }
            "B_ONLY" -> {
                if (exp.paidBy == "Partner A") {
                    partnerBOwesPartnerA += amount
                }
            }
        }
    }

    val netBalance = partnerBOwesPartnerA - partnerAOwesPartnerB

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Balance Banner Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (netBalance >= 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("balance_card"),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SHARED COST BALANCE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (netBalance >= 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (netBalance == 0.0) {
                            "You are perfectly even!"
                        } else if (netBalance > 0) {
                            "Partner B owes Partner A\n${formatCurrency(netBalance)}"
                        } else {
                            "Partner A owes Partner B\n${formatCurrency(Math.abs(netBalance))}"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = if (netBalance >= 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onSettleUpClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (netBalance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.testTag("settle_up_button")
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = "Settle Up")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Settle Up Balances")
                    }
                }
            }
        }

        // Reminders & Upcoming Recurring Headers
        item {
            Text(
                text = "Active Bills & Due Reminders",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Horizontal billing triggers
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReminderChip(
                    title = "Rent & Water Due",
                    dueDate = "in 3 days",
                    onClick = { onTriggerReminder("Rent & Water Bill Reminder scheduled!") }
                )
                ReminderChip(
                    title = "City Power Electric",
                    dueDate = "in 5 days",
                    onClick = { onTriggerReminder("Electric Bill reminder scheduled!") }
                )
                ReminderChip(
                    title = "Gym Joint Membership",
                    dueDate = "in 9 days",
                    onClick = { onTriggerReminder("Gym membership dues reminder set!") }
                )
            }
        }

        // Expenses List header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${expenses.size} total",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        if (expenses.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = "Empty list",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No expenses recorded yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            items(expenses) { exp ->
                ExpenseItemCard(
                    expense = exp,
                    onDelete = { onDeleteExpense(exp) }
                )
            }
        }
    }
}

@Composable
fun ReminderChip(
    title: String,
    dueDate: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .width(180.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.NotificationsActive,
                    contentDescription = "Notification alert",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "DUE $dueDate",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap to set reminder",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun ExpenseItemCard(
    expense: Expense,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(getCategoryColor(expense.category).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(expense.category),
                    contentDescription = expense.category,
                    tint = getCategoryColor(expense.category),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = expense.description,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (expense.isBankSynced) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Synced from bank",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    if (expense.isRecurring) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = "Recurring",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Paid by ${expense.paidBy} • Split: ${getSplitLabel(expense.splitRule)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = formatDate(expense.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount & Actions
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(expense.amount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete expense",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ==================== DASHBOARD / INSIGHTS TAB ====================

@Composable
fun InsightsTab(
    expenses: List<Expense>,
    goals: List<SavingsGoal>,
    budgetLimit: Double,
    aiRecommendations: String,
    isAiLoading: Boolean,
    onGenerateAi: () -> Unit,
    onExportReport: () -> Unit,
    onUpdateBudget: (Double) -> Unit
) {
    val budgetValue = budgetLimit
    val totalSpent = expenses.sumOf { if (it.category != "Settle Up") it.amount else 0.0 }
    val budgetProgress = if (budgetValue > 0) (totalSpent / budgetValue).toFloat() else 0f

    // Calculate category breakdowns
    val categoryTotals = expenses
        .filter { it.category != "Settle Up" }
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }

    val categories = listOf("Housing", "Food", "Entertainment", "Utilities", "Transport", "Other")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Budget Circle Progress & Gauge
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "MONTHLY JOINT BUDGET",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))

                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
                    CircularProgressIndicator(
                        progress = { budgetProgress.coerceAtMost(1f) },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 14.dp,
                        color = if (budgetProgress > 0.9f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(budgetProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "spent",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "${formatCurrency(totalSpent)} of ${formatCurrency(budgetValue)} limit",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))
                // Quick adjustment of budget slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Budget Goal", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Slider(
                        value = budgetValue.toFloat(),
                        onValueChange = { onUpdateBudget(it.toDouble()) },
                        valueRange = 500f..8000f,
                        steps = 15,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    )
                    Text(formatCurrency(budgetValue), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Custom Visual Spending Bar Chart (Compose Canvas)
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Spending Breakdown by Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Canvas Drawing
                val barColors = categories.map { getCategoryColor(it) }
                val maxCategoryAmount = categoryTotals.values.maxOrNull() ?: 1.0

                Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                    val width = size.width
                    val height = size.height
                    val barSpacing = 16.dp.toPx()
                    val barWidth = (width - (barSpacing * (categories.size - 1))) / categories.size

                    categories.forEachIndexed { index, cat ->
                        val amount = categoryTotals[cat] ?: 0.0
                        val barHeightFactor = (amount / maxCategoryAmount).toFloat()
                        val barHeight = (height * 0.8f) * barHeightFactor

                        // Draw Bar
                        val left = index * (barWidth + barSpacing)
                        val top = height - barHeight - 20.dp.toPx()
                        
                        drawRoundRect(
                            color = barColors[index],
                            topLeft = Offset(left, top),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )
                    }
                }

                // Legend row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    categories.forEach { cat ->
                        val amount = categoryTotals[cat] ?: 0.0
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(getCategoryColor(cat))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(cat, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(formatCurrency(amount), fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }

        // DuoShare AI Assistant Panel
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = "AI Coach",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "DuoShare AI Financial Coach",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Get real-time, personalized joint savings strategies and action items powered by Gemini 3.5 Flash.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (isAiLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Analyzing goals and bills...", style = MaterialTheme.typography.bodyMedium)
                    }
                } else if (aiRecommendations.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = aiRecommendations,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = onGenerateAi,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("generate_ai_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "Gemini Recommendations Icon")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (aiRecommendations.isEmpty()) "Get Savings Recommendations" else "Refresh AI Coach")
                }
            }
        }

        // Export Section
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Export Tax Ledger",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Generate a full CSV sheet of split balances for filing joint deductions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = onExportReport,
                    modifier = Modifier.testTag("export_tax_button")
                ) {
                    Icon(Icons.Default.Download, contentDescription = "CSV Download Icon")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export")
                }
            }
        }
    }
}

// ==================== COUPLES GOALS TAB ====================

@Composable
fun CouplesGoalsTab(
    goals: List<SavingsGoal>,
    milestones: List<Milestone>,
    onContribute: (SavingsGoal, Double) -> Unit,
    onDeleteGoal: (SavingsGoal) -> Unit,
    onToggleMilestone: (Milestone) -> Unit,
    onDeleteMilestone: (Milestone) -> Unit,
    onAddMilestoneClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Shared Savings Tracker",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Put aside funds for your future milestones together.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        if (goals.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.TrendingUp, contentDescription = "Savings empty icon", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No goals added yet. Tap 'New Goal' below!", color = MaterialTheme.colorScheme.outline, textAlign = TextAlign.Center)
                }
            }
        } else {
            goals.forEach { goal ->
                GoalItemCard(
                    goal = goal,
                    onSave = { amount -> onContribute(goal, amount) },
                    onDelete = { onDeleteGoal(goal) }
                )
            }
        }

        // Timeline Milestones Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Joint Milestones",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Important life & financial check-ins for the couple.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            IconButton(
                onClick = onAddMilestoneClick,
                modifier = Modifier.testTag("add_milestone_button")
            ) {
                Icon(Icons.Default.AddCircleOutline, contentDescription = "Add milestone")
            }
        }

        if (milestones.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Milestones empty icon", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No joint milestones yet.", color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            milestones.forEach { milestone ->
                MilestoneItemRow(
                    milestone = milestone,
                    onToggle = { onToggleMilestone(milestone) },
                    onDelete = { onDeleteMilestone(milestone) }
                )
            }
        }
    }
}

@Composable
fun GoalItemCard(
    goal: SavingsGoal,
    onSave: (Double) -> Unit,
    onDelete: () -> Unit
) {
    val progress = (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
    var contributeAmountText by remember { mutableStateOf("50") }

    Card(
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = goal.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Category: ${goal.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Goal",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${formatCurrency(goal.currentAmount)} saved",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Goal: ${formatCurrency(goal.targetAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick deposit action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = contributeAmountText,
                    onValueChange = { contributeAmountText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(80.dp),
                    label = { Text("Amount") },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    singleLine = true
                )
                Button(
                    onClick = {
                        val amount = contributeAmountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            onSave(amount)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Savings, contentDescription = "Deposit Icon")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Contribution")
                }
            }
        }
    }
}

@Composable
fun MilestoneItemRow(
    milestone: Milestone,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = milestone.completed,
                onCheckedChange = { onToggle() }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = milestone.title,
                    style = if (milestone.completed) {
                        MaterialTheme.typography.bodyMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                    } else {
                        MaterialTheme.typography.bodyMedium
                    },
                    fontWeight = FontWeight.Bold,
                    color = if (milestone.completed) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                )
                if (milestone.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = milestone.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Due: ${formatDate(milestone.dueDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 10.sp
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete milestone", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ==================== BANK SYNC & SECURITY TAB ====================

@Composable
fun BankSecurityTab(
    connections: List<BankConnection>,
    onLinkBankClick: () -> Unit,
    onSyncBank: (BankConnection) -> Unit,
    onDeleteBank: (BankConnection) -> Unit
) {
    val isEncryptionSupported = SecurityManager.isEncryptedSecurely()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // End-to-End Encryption Banner
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Shield Active",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "End-to-End Encrypted Storage",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "All personal ledger entries, bank connection configurations, and milestone notes are cryptographically signed and encrypted using hardware-backed AES-256 GCM prior to writing to SQLite storage. The encryption key never leaves the Android hardware Keystore.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Active Icon",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Android Keystore Alias initialized • Security Active",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        // Bank Account Sync header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Secure Bank Account Syncing",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Link accounts for automated transaction synchronization.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        if (connections.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = "No Bank account connected",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No linked banking connections.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            connections.forEach { conn ->
                BankConnectionCard(
                    connection = conn,
                    onSync = { onSyncBank(conn) },
                    onDelete = { onDeleteBank(conn) }
                )
            }
        }

        Button(
            onClick = onLinkBankClick,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("link_bank_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Link, contentDescription = "Link Icon")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Link New Bank Account")
        }
    }
}

@Composable
fun BankConnectionCard(
    connection: BankConnection,
    onSync: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccountBalance,
                        contentDescription = "Bank logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = connection.institutionName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.LinkOff,
                        contentDescription = "Disconnect account",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = connection.accountName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Real-time Balance: ${formatCurrency(connection.balance)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Synced: ${formatDate(connection.lastSynced)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 10.sp
                )
                Button(
                    onClick = onSync,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Sync, contentDescription = "Sync", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sync Now", fontSize = 12.sp)
                }
            }
        }
    }
}


// ==================== HELPER METHODS ====================

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    return format.format(amount)
}

fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "Housing" -> Icons.Default.Home
        "Food" -> Icons.Default.Restaurant
        "Entertainment" -> Icons.Default.Movie
        "Utilities" -> Icons.Default.ElectricBolt
        "Transport" -> Icons.Default.DirectionsCar
        "Settle Up" -> Icons.Default.CheckCircle
        else -> Icons.Default.ShoppingBag
    }
}

fun getCategoryColor(category: String): Color {
    return when (category) {
        "Housing" -> Color(0xFF5C6BC0) // Indigo
        "Food" -> Color(0xFFEF5350) // Coral
        "Entertainment" -> Color(0xFFAB47BC) // Amethyst
        "Utilities" -> Color(0xFFFFCA28) // Amber
        "Transport" -> Color(0xFF26A69A) // Teal
        "Settle Up" -> Color(0xFF66BB6A) // Mint Green
        else -> Color(0xFF8D6E63) // Sage/Brown
    }
}

fun getSplitLabel(rule: String): String {
    return when (rule) {
        "EQUAL" -> "Equally (50/50)"
        "60_40" -> "60% A / 40% B"
        "70_30" -> "70% A / 30% B"
        "A_ONLY" -> "100% Partner A"
        "B_ONLY" -> "100% Partner B"
        else -> "Custom split"
    }
}


// ==================== DIALOG IMPLEMENTATIONS ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String, String, String, Boolean, String?) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var paidBy by remember { mutableStateOf("Partner A") }
    var splitRule by remember { mutableStateOf("EQUAL") }
    var category by remember { mutableStateOf("Food") }
    var isRecurring by remember { mutableStateOf(false) }
    var recurrencePeriod by remember { mutableStateOf("MONTHLY") }

    val categories = listOf("Housing", "Food", "Entertainment", "Utilities", "Transport", "Other")
    val splitRules = listOf("EQUAL", "60_40", "70_30", "A_ONLY", "B_ONLY")
    val paidByList = listOf("Partner A", "Partner B", "Joint")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Add Shared Expense",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_desc_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_amount_input"),
                    singleLine = true
                )

                // Category selection dropdown-like custom Row
                Text("Category", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                // Paid By selection
                Text("Who Paid?", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    paidByList.forEach { partner ->
                        FilterChip(
                            selected = paidBy == partner,
                            onClick = { paidBy = partner },
                            label = { Text(partner) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Split rules
                Text("Split Shares", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    splitRules.forEach { rule ->
                        FilterChip(
                            selected = splitRule == rule,
                            onClick = { splitRule = rule },
                            label = { Text(getSplitLabel(rule)) }
                        )
                    }
                }

                // Recurring toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isRecurring, onCheckedChange = { isRecurring = it })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Is Recurring Bill?", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                if (isRecurring) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("WEEKLY", "MONTHLY").forEach { period ->
                            FilterChip(
                                selected = recurrencePeriod == period,
                                onClick = { recurrencePeriod = period },
                                label = { Text(period) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull() ?: 0.0
                            if (description.isNotEmpty() && amount > 0) {
                                onConfirm(
                                    description,
                                    amount,
                                    paidBy,
                                    splitRule,
                                    category,
                                    isRecurring,
                                    if (isRecurring) recurrencePeriod else null
                                )
                            }
                        },
                        modifier = Modifier.testTag("dialog_add_button")
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Double, Int, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    var currentText by remember { mutableStateOf("0") }
    var targetDays by remember { mutableStateOf(180) }
    var category by remember { mutableStateOf("Travel") }

    val categories = listOf("Travel", "Furniture", "Savings", "Housing", "Other")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "Create Savings Goal", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Target Amount ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = currentText,
                    onValueChange = { currentText = it },
                    label = { Text("Starting Balance ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Category", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val target = targetText.toDoubleOrNull() ?: 0.0
                            val current = currentText.toDoubleOrNull() ?: 0.0
                            if (name.isNotEmpty() && target > 0) {
                                onConfirm(name, target, current, targetDays, category)
                            }
                        }
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

@Composable
fun AddMilestoneDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var targetDaysText by remember { mutableStateOf("30") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "Add Joint Milestone", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Milestone Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = targetDaysText,
                    onValueChange = { targetDaysText = it },
                    label = { Text("Due in how many days?") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Private checklist/notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val days = targetDaysText.toIntOrNull() ?: 30
                            if (title.isNotEmpty()) {
                                onConfirm(title, days, notes)
                            }
                        }
                    ) {
                        Text("Add Milestone")
                    }
                }
            }
        }
    }
}

@Composable
fun LinkBankDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, String) -> Unit
) {
    var institution by remember { mutableStateOf("Chase Bank Sandbox") }
    var accountName by remember { mutableStateOf("Duo Premier Checking") }
    var balanceText by remember { mutableStateOf("5400.00") }
    var credentials by remember { mutableStateOf("duo_sandbox_link_token") }

    val institutions = listOf("Chase Bank Sandbox", "Wells Fargo Sandbox", "DuoBank Shared API")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "Link Bank Account (Secure Plaid Flow)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = "DuoShare uses a sandboxed Plaid integration. Linking an account imports 5 realistic transactions encrypted via local Keystore.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Text("Select Institution", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    institutions.forEach { inst ->
                        FilterChip(
                            selected = institution == inst,
                            onClick = { institution = inst },
                            label = { Text(inst) }
                        )
                    }
                }

                OutlinedTextField(
                    value = accountName,
                    onValueChange = { accountName = it },
                    label = { Text("Account Custom Label") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text("Starting Balance ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = credentials,
                    onValueChange = { credentials = it },
                    label = { Text("Secure Link Token") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val balance = balanceText.toDoubleOrNull() ?: 0.0
                            if (accountName.isNotEmpty()) {
                                onConfirm(institution, accountName, balance, credentials)
                            }
                        }
                    ) {
                        Text("Connect Securely")
                    }
                }
            }
        }
    }
}

@Composable
fun SettleUpDialog(
    expenses: List<Expense>,
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var partnerAOwesPartnerB = 0.0
    var partnerBOwesPartnerA = 0.0

    for (exp in expenses) {
        if (exp.category == "Settle Up") continue
        
        val amount = exp.amount
        when (exp.splitRule) {
            "EQUAL" -> {
                if (exp.paidBy == "Partner A") {
                    partnerBOwesPartnerA += amount / 2.0
                } else if (exp.paidBy == "Partner B") {
                    partnerAOwesPartnerB += amount / 2.0
                }
            }
            "60_40" -> {
                if (exp.paidBy == "Partner A") {
                    partnerBOwesPartnerA += amount * 0.4
                } else if (exp.paidBy == "Partner B") {
                    partnerAOwesPartnerB += amount * 0.6
                }
            }
            "70_30" -> {
                if (exp.paidBy == "Partner A") {
                    partnerBOwesPartnerA += amount * 0.3
                } else if (exp.paidBy == "Partner B") {
                    partnerAOwesPartnerB += amount * 0.7
                }
            }
            "A_ONLY" -> {
                if (exp.paidBy == "Partner B") {
                    partnerAOwesPartnerB += amount
                }
            }
            "B_ONLY" -> {
                if (exp.paidBy == "Partner A") {
                    partnerBOwesPartnerA += amount
                }
            }
        }
    }

    val netBalance = partnerBOwesPartnerA - partnerAOwesPartnerB
    val owesWho = if (netBalance >= 0) "Partner A" else "Partner B"
    val settleAmount = Math.abs(netBalance)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Settle Up Dues", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                if (settleAmount == 0.0) {
                    Text(
                        "You don't owe each other anything! All balanced.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                        Text("Done")
                    }
                } else {
                    Text(
                        text = if (netBalance > 0) {
                            "To settle balances, Partner B will reimburse Partner A ${formatCurrency(settleAmount)}."
                        } else {
                            "To settle balances, Partner A will reimburse Partner B ${formatCurrency(settleAmount)}."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                        Button(
                            onClick = { onConfirm(owesWho, settleAmount) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Record Payment")
                        }
                    }
                }
            }
        }
    }
}
