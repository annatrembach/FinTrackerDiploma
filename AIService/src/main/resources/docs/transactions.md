# Transactions

## What is a transaction?
A transaction represents any financial operation — income or expense — in the system.
Each transaction belongs to a specific user and has a category, amount, date, and type.

## Creating a transaction
To create a transaction, provide:
- **type**: INCOME or EXPENSE
- **category**: e.g., FOOD, TRANSPORT, SALARY, ENTERTAINMENT, HEALTH
- **amount**: positive decimal number
- **date**: ISO-8601 format (e.g., 2024-01-15)
- **description**: optional text note

Example: "Add expense of 150 for food yesterday"

## Viewing transactions
- "Show my transactions" — returns all your transactions
- "Show transactions by category food" — filters by category
- "Show income transactions" — filters by type
- "Sort transactions by amount" — sortBy parameter

## Editing a transaction
You can update amount, category, description, or date of any existing transaction.
Example: "Update transaction 42 change amount to 200"

## Deleting a transaction
Example: "Delete transaction 42"
The action is permanent and cannot be undone.

## Balance
Your balance = total INCOME minus total EXPENSE.
Example: "What is my current balance?"

## Monthly summary
Shows total income and expenses grouped by month.
Example: "Show my monthly summary"