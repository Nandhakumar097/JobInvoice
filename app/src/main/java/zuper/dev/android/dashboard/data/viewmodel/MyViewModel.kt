package zuper.dev.android.dashboard.data.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import zuper.dev.android.dashboard.data.DataRepository
import zuper.dev.android.dashboard.data.model.InvoiceApiModel
import zuper.dev.android.dashboard.data.model.JobApiModel
import javax.inject.Inject

@HiltViewModel
class MyViewModel @Inject constructor(private var dataRepository: DataRepository) : ViewModel(){

    fun observeJobList() : Flow<List<JobApiModel>> {
        return dataRepository.observeJobs()
    }

    fun observeInvoiceList() : Flow<List<InvoiceApiModel>> {
        return dataRepository.observeInvoices()
    }


}