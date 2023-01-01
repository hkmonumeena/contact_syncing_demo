package com.developermonu.contactsyncing

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import com.developermonu.contactsyncing.dataclasses.Contact
import com.developermonu.contactsyncing.ui.theme.ContactSyncingTheme
import com.developermonu.contactsyncing.viewmodels.ContactsViewModel
import java.io.File
import java.io.FileWriter
import java.io.IOException

class MainActivity : ComponentActivity() {
    private val contactsViewModel by viewModels<ContactsViewModel>()
    private val CONTACTS_READ_REQ_CODE = 100
    private lateinit var outputDirectory: File
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ContactSyncingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val listOfContacts = contactsViewModel.getAllContacts.collectAsState().value
                    ContactsUi(arrayList = listOfContacts)
                    if (hasPermission(Manifest.permission.READ_CONTACTS)) {
                        contactsViewModel.fetchContacts()
                    } else {
                        requestPermissionWithRationale(
                            Manifest.permission.READ_CONTACTS,
                            CONTACTS_READ_REQ_CODE,
                            "Contact read permission required"
                        )
                    }
                }
            }
        }

        createDummyContacts()
    }

    fun createDummyContacts() {
        outputDirectory = getOutputDirectory()
        val photoFile = File(outputDirectory, "contacts" + ".vcf")
        val writer = FileWriter(photoFile)
        for (i in 1 until 5000) {
            val contact = """BEGIN:VCARD
VERSION:2.1
N:;Contact-$i;;;
FN:Contact-$i
TEL;CELL:198$i
TEL;CELL:198$i
TEL;HOME:199$i
EMAIL;HOME:test$i@gmail.com
END:VCARD
"""
            writer.append(contact)
        }
        writer.flush()
        writer.close()
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, "identify").apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CONTACTS_READ_REQ_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            contactsViewModel.fetchContacts()
        }
    }
}

@Composable
fun ContactsUi(arrayList: ArrayList<Contact>) {
    LazyColumn(content = {
        items(arrayList.size) {
            NameAndNumber(contact = arrayList[it])
        }
    })
}

@Composable
fun NameAndNumber(contact: Contact) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .background(color = Color.Gray)
    ) {
        Text(text = contact.name)
        Text(text = contact.numbers.toString().replace("[", "").replace("]", ""))
        Text(text = contact.emails.toString().replace("[", "").replace("]", ""))
    }
}
