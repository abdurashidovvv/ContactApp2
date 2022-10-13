import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abdurashidov.contactapp.R
import com.abdurashidov.contactapp.databinding.RvItemBinding
import com.abdurashidov.contactapp.models.Contact
import kotlinx.android.synthetic.main.rv_item.view.*

class RvAdapter(var list: List<Contact>)
    :RecyclerView.Adapter<RvAdapter.Vh>(){

    inner class Vh(var itemView:View):RecyclerView.ViewHolder(itemView){
        fun onBind(contact: Contact){
            itemView.txt_name.text = contact.name
            itemView.txt_number.text = contact.number
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(LayoutInflater.from(parent.context).inflate(R.layout.rv_item, parent, false))
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int = list.size

}