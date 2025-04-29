package com.example.thunderscope_frontend.ui.customview

import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import androidx.fragment.app.DialogFragment
import com.example.thunderscope_frontend.R
import com.example.thunderscope_frontend.databinding.DialogImagePreviewBinding

class ImagePreviewDialogFragment : DialogFragment() {

    private var _binding: DialogImagePreviewBinding? = null
    private val binding get() = _binding!!

    private var bitmap: Bitmap? = null

    companion object {
        fun newInstance(bitmap: Bitmap?): ImagePreviewDialogFragment {
            val fragment = ImagePreviewDialogFragment()
            fragment.bitmap = bitmap
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogImagePreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bitmap?.let {
            Log.e("FTEST", "aa1: $bitmap" )

            binding.ivPreview.setImageBitmap(bitmap)
        } ?: {
            Log.e("FTEST", "aa2: $bitmap" )
        }

        binding.btnClose.setOnClickListener {
            dismiss()
        }

        // Close the dialog when user clicks outside (optional)
        binding.root.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            val metrics = resources.displayMetrics
            val width = (metrics.widthPixels * 0.9).toInt()
            val height = (metrics.heightPixels * 0.9).toInt()
            window.setLayout(width, height)
            window.setGravity(Gravity.CENTER)
            window.setDimAmount(0.7f) // Darken background
        }
    }

    override fun dismiss() {
        val view = dialog?.window?.decorView?.findViewById<View>(android.R.id.content)
        view?.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.slide_out_bottom))
        view?.postDelayed({
            super.dismiss()
        }, 300)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
