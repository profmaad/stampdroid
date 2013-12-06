# StampDroid

StampDroid is an Android trading app for the [Bitstamp](https://www.bistamp.net) [Bitcoin](http://www.bitcoin.org) exchange.

## Build & Install

StampDroid uses the Android ant-based build system and requires at least API level 18 (Android 4.3 Jelly Bean).
`ant` must be installed and in the `PATH` for the following steps to work.

* To generate the `local.properties` file: `android update project -p .` (only needs to be done once per system)
* To build a debug binary: `ant debug`
* To install the debug binary on an emulator or device: `ant installd`

## Getting Started

On first start, the app requests your API credentials for the Bitstamp API.
These credentials can be found on the Bitstamp website under "Account" -> "Security" -> "API Access", once logged in.

Please note that the API key must be activated. The following API permissions must be given to the API key to use all features of StampDroid:

* Account balance (to display account balances)
* Open orders (to display list of currently open orders)
* User transactions (to display past user transactions)
* Buy limit order (to enable creating orders)
* Sell limit order (to enable creating orders)
* Cancel order (to enable creating orders)

StampDroid can be used in a "read-only" mode by supplying an API key that only has the first three permissions. This allows displaying all account information, but disables creating/canceling orders. Please note that these actions will still be displayed in the GUI, but won't function. Since Bitstamp doesn't offer the ability to list the permissions for a given API key, there is no sane way of discovering whether or not order creation is enabled.

# Copyright
(c) 2013 Maximilian Wolter