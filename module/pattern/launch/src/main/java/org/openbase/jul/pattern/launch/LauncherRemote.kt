package org.openbase.jul.pattern.launch

import org.openbase.jul.communication.controller.AbstractIdentifiableRemote
import org.openbase.type.execution.LauncherDataType.LauncherData

class LauncherRemote: AbstractIdentifiableRemote<LauncherData>(LauncherData::class.java)
