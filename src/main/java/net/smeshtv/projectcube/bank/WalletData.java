package net.smeshtv.projectcube.wallet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.smeshtv.projectcube.ProjectCube;

import java.util.*;

public class WalletData {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ProjectCube.MODID);

    private static final Codec<WalletData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.LONG.optionalFieldOf("balance", 0L).forGetter(b -> b.balance),
                    Codec.STRING.optionalFieldOf("walletId", "").forGetter(b -> b.walletId),
                    Codec.STRING.optionalFieldOf("recoveryCode", "").forGetter(b -> b.recoveryCode),
                    Transaction.CODEC.listOf().optionalFieldOf("transactions", new ArrayList<>())
                            .forGetter(b -> b.transactions)
            ).apply(instance, WalletData::new)
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<WalletData>> WALLET_DATA =
            ATTACHMENTS.register("wallet_data",
                    () -> AttachmentType.<WalletData>builder(() -> new WalletData())
                            .serialize(CODEC)
                            .build()
            );

    private long balance = 0;
    private String walletId = "";
    private String recoveryCode = "";
    private final List<Transaction> transactions = new ArrayList<>();

    public WalletData() {
        // При создании нового кошелька генерируем ID и код восстановления
        generateNewWallet();
    }

    public WalletData(long balance, String walletId, String recoveryCode, List<Transaction> transactions) {
        this.balance = balance;
        this.walletId = walletId;
        this.recoveryCode = recoveryCode;
        this.transactions.addAll(transactions);
    }

    // === Основные методы ===
    public long getBalance() {
        return balance;
    }

    public String getWalletId() {
        return walletId;
    }

    public String getRecoveryCode() {
        return recoveryCode;
    }

    public boolean addBalance(long amount, String source) {
        if (amount <= 0) return false;
        balance += amount;
        addTransaction("+", source, amount, balance);
        return true;
    }

    public boolean removeBalance(long amount, String source) {
        if (amount <= 0 || amount > balance) return false;
        balance -= amount;
        addTransaction("-", source, -amount, balance);
        return true;
    }

    public boolean transferTo(WalletData target, long amount, String source) {
        if (amount <= 0 || amount > balance) return false;

        this.balance -= amount;
        this.addTransaction("→", "Перевод " + source, -amount, balance);

        target.balance += amount;
        target.addTransaction("←", "От " + source, amount, target.balance);

        return true;
    }

    public List<Transaction> getTransactions(int limit) {
        int start = Math.max(0, transactions.size() - limit);
        return new ArrayList<>(transactions.subList(start, transactions.size()));
    }

    private void addTransaction(String type, String description, long amount, long newBalance) {
        transactions.add(new Transaction(type, description, amount, newBalance, System.currentTimeMillis()));
        if (transactions.size() > 100) {
            transactions.remove(0);
        }
    }

    // Генерация нового кошелька
    private void generateNewWallet() {
        this.walletId = generateWalletId();
        this.recoveryCode = generateRecoveryCode();
    }

    // Восстановление кошелька по коду
    public boolean recoverWallet(String recoveryCode, WalletData backedUpData) {
        if (this.recoveryCode.equals(recoveryCode) || this.walletId.isEmpty()) {
            // Восстанавливаем данные из резервной копии
            this.balance = backedUpData.balance;
            this.walletId = backedUpData.walletId;
            this.recoveryCode = backedUpData.recoveryCode;
            this.transactions.clear();
            this.transactions.addAll(backedUpData.transactions);
            return true;
        }
        return false;
    }

    // Генерация уникального ID кошелька
    private String generateWalletId() {
        // Генерация ID: WALLET + случайные цифры
        Random random = new Random();
        int randomNum = 100000 + random.nextInt(900000);
        return "WALLET-" + randomNum;
    }

    // Генерация кода восстановления
    private String generateRecoveryCode() {
        // Генерация кода: 8 символов (цифры и буквы)
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    public static WalletData get(Player player) {
        return player.getData(WALLET_DATA);
    }

    // === Внутренний класс Transaction ===
    public static class Transaction {
        public static final Codec<Transaction> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.fieldOf("type").forGetter(t -> t.type),
                        Codec.STRING.fieldOf("description").forGetter(t -> t.description),
                        Codec.LONG.fieldOf("amount").forGetter(t -> t.amount),
                        Codec.LONG.fieldOf("newBalance").forGetter(t -> t.newBalance),
                        Codec.LONG.fieldOf("timestamp").forGetter(t -> t.timestamp)
                ).apply(instance, Transaction::new)
        );

        public final String type;
        public final String description;
        public final long amount;
        public final long newBalance;
        public final long timestamp;

        public Transaction(String type, String description, long amount, long newBalance, long timestamp) {
            this.type = type;
            this.description = description;
            this.amount = amount;
            this.newBalance = newBalance;
            this.timestamp = timestamp;
        }

        public String getFormattedTime() {
            long diff = System.currentTimeMillis() - timestamp;
            long minutes = diff / 60000;
            long hours = minutes / 60;
            long days = hours / 24;

            if (days > 0) return days + "д назад";
            if (hours > 0) return hours + "ч назад";
            if (minutes > 0) return minutes + "м назад";
            return "Только что";
        }
    }
}