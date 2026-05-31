package com.mybank.tui;

import com.mybank.domain.*;
import com.mybank.data.DataSource; // Перевір назву пакета DataSource з твоєї 8-ї лаби
import jexer.TAction;
import jexer.TApplication;
import jexer.TField;
import jexer.TText;
import jexer.TWindow;
import jexer.event.TMenuEvent;
import jexer.menu.TMenu;
import java.io.File;

public class TUIdemo extends TApplication {

    private static final int ABOUT_APP = 2000;
    private static final int CUST_INFO = 2010;

    public static void main(String[] args) throws Exception {
        String dataFilePath = "test.dat";

        File dataFile = new File(dataFilePath);
        if (dataFile.exists()) {
            DataSource dataSource = new DataSource(dataFilePath);
            dataSource.loadData();
        } else {
            System.err.println("Помилка: Файл " + dataFile.getAbsolutePath() + " не знайдено!");
            Bank bank = Bank.getBank();
            bank.addCustomer("Іван", "Зубарев");
            bank.getCustomer(0).addAccount(new SavingsAccount(300.00, 0.06));
            bank.addCustomer("Лоліта", "Набокова");
            bank.getCustomer(1).addAccount(new CheckingAccount(2000.00, 25.00));
        }

        TUIdemo tdemo = new TUIdemo();
        (new Thread(tdemo)).start();
    }

    public TUIdemo() throws Exception {
        super(BackendType.SWING);

        addToolMenu();
        TMenu fileMenu = addMenu("&File");
        fileMenu.addItem(CUST_INFO, "&Customer Info");
        fileMenu.addDefaultItem(TMenu.MID_SHELL);
        fileMenu.addSeparator();
        fileMenu.addDefaultItem(TMenu.MID_EXIT);

        addWindowMenu();

        TMenu helpMenu = addMenu("&Help");
        helpMenu.addItem(ABOUT_APP, "&About...");

        setFocusFollowsMouse(true);
        ShowCustomerDetails();
    }

    @Override
    protected boolean onMenu(TMenuEvent menu) {
        if (menu.getId() == ABOUT_APP) {
            messageBox("About",
                    "\t\t\t\t\t   Just a simple Jexer demo.\n\nCopyright \u00A9 2019 Alexander \'Taurus\' Babich")
                    .show();
            return true;
        }
        if (menu.getId() == CUST_INFO) {
            ShowCustomerDetails();
            return true;
        }
        return super.onMenu(menu);
    }

    private void ShowCustomerDetails() {
        TWindow custWin = addWindow("Customer Window", 2, 1, 40, 12, TWindow.NOZOOMBOX);
        custWin.newStatusBar("Enter valid customer number and press Show...");

        custWin.addLabel("Enter customer number: ", 2, 2);
        TField custNo = custWin.addField(24, 2, 3, false);
        TText details = custWin.addText("Owner Name: \nAccount Type: \nAccount Balance: ", 2, 4, 38, 5);

        custWin.addButton("&Show", 28, 2, new TAction() {
            @Override
            public void DO() {
                try {
                    int custNum = Integer.parseInt(custNo.getText());
                    Bank bank = Bank.getBank();

                    if (custNum >= 0 && custNum < bank.getNumberOfCustomers()) {
                        Customer customer = bank.getCustomer(custNum);

                        if (customer.getNumberOfAccounts() > 0) {
                            Account account = customer.getAccount(0);

                            String acctType = "Unknown";
                            if (account instanceof SavingsAccount) {
                                acctType = "Savings";
                            } else if (account instanceof CheckingAccount) {
                                acctType = "Checking";
                            } else {
                                acctType = "Account";
                            }

                            String info = String.format(
                                    "Owner Name: %s %s (id=%d)\nAccount Type: '%s'\nAccount Balance: $%.2f",
                                    customer.getFirstName(), customer.getLastName(), custNum, acctType,
                                    account.getBalance());

                            details.setText(info);
                        } else {
                            details.setText("Owner Name: " + customer.getFirstName() + " " + customer.getLastName()
                                    + "\nNo accounts found for this customer.");
                        }
                    } else {
                        messageBox("Error", "Customer with ID " + custNum + " does not exist!").show();
                    }
                } catch (NumberFormatException e) {
                    messageBox("Error", "You must provide a valid customer number!").show();
                } catch (Exception e) {
                    messageBox("Error", "An unexpected error occurred: " + e.getMessage()).show();
                }
            }
        });
    }
}