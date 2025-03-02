package me.khajiitos.jackseconomy.blockentity;

import me.khajiitos.jackseconomy.data.price.ItemDescription;

public interface IImporterBlockEntity extends ITransactionMachineBlockEntity {
    void selectItem(ItemDescription itemDescription);
    ItemDescription getSelectedItem();
}
