package io.github.sds100.keymapper.domain.packages

import io.github.sds100.keymapper.domain.utils.State
import kotlinx.coroutines.flow.Flow

/**
 * Created by sds100 on 23/03/2021.
 */
class GetPackagesUseCaseImpl(private val adapter: PackageManagerAdapter) : GetPackagesUseCase {
    override val installedPackages = adapter.installedPackages
}

interface GetPackagesUseCase {
    val installedPackages: Flow<State<List<PackageInfo>>>
}