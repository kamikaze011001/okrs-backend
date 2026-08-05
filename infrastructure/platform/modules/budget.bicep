targetScope = 'resourceGroup'

param name string
param amount int
param alertEmail string = ''
param startDate string = utcNow('yyyy-MM-01T00:00:00Z')

var notifications = empty(alertEmail) ? {} : {
  Threshold50: {
    enabled: true
    operator: 'GreaterThan'
    threshold: 50
    contactEmails: [
      alertEmail
    ]
  }
  Threshold90: {
    enabled: true
    operator: 'GreaterThan'
    threshold: 90
    contactEmails: [
      alertEmail
    ]
  }
}

resource budget 'Microsoft.Consumption/budgets@2023-11-01' = {
  name: name
  properties: {
    amount: amount
    category: 'Cost'
    timeGrain: 'Monthly'
    timePeriod: {
      startDate: startDate
    }
    notifications: notifications
  }
}
