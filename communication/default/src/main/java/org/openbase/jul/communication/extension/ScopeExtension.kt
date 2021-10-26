package org.openbase.jul.communication.extension

import org.openbase.type.communication.ScopeType.Scope

fun Scope.concat(scope: Scope) = toBuilder().addAllComponent(scope.componentList).build()
