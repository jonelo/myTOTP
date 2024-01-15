package uz.khurozov.mytotp.fx.dialog;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import uz.khurozov.mytotp.App;
import uz.khurozov.mytotp.store.TotpData;
import uz.khurozov.totp.Algorithm;
import uz.khurozov.totp.TOTP;

public class TotpDataDialog extends Dialog<TotpData> {

    public TotpDataDialog(Type type) {
        setTitle(App.TITLE);
        getDialogPane().setMinWidth(350);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox vBox;

        switch (type) {
            case MANUAL -> {
                TextField name = new TextField();
                Label nameLabel = new Label("Name:");
                nameLabel.setLabelFor(name);

                TextField secret = new TextField();
                Label secretLabel = new Label("Secret:");
                secretLabel.setLabelFor(secret);

                Spinner<Integer> digits = new Spinner<>(
                        new SpinnerValueFactory.IntegerSpinnerValueFactory(6, 10)
                );
                Label digitsLabel = new Label("Digits:");
                digitsLabel.setLabelFor(digits);

                ChoiceBox<Algorithm> algorithm = new ChoiceBox<>();
                algorithm.setItems(FXCollections.observableArrayList(Algorithm.values()));
                Label algorithmLabel = new Label("Algorithm:");
                algorithmLabel.setLabelFor(algorithm);

                Spinner<Integer> period = new Spinner<>(
                        new SpinnerValueFactory.IntegerSpinnerValueFactory(30, 600, 30, 5)
                );
                Label periodLabel = new Label("Period (seconds):");
                periodLabel.setLabelFor(period);

                TitledPane advanced = new TitledPane("Advanced", new VBox(
                        10, algorithmLabel, algorithm, digitsLabel, digits, periodLabel, period
                ));
                advanced.setExpanded(true);

                getDialogPane().lookupButton(ButtonType.OK).disableProperty().bind(new BooleanBinding() {
                    {
                        super.bind(name.textProperty(), secret.textProperty());
                    }

                    @Override
                    protected boolean computeValue() {
                        return name.getText().isBlank() || secret.getText().isBlank();
                    }
                });

                setResultConverter(buttonType -> {
                    if (buttonType == ButtonType.OK) {
                        return new TotpData(
                                name.getText().trim(),
                                secret.getText().trim(),
                                algorithm.getValue(),
                                digits.getValue(),
                                period.getValue()
                        );
                    }
                    return null;
                });

                setOnShowing(e -> Platform.runLater(() -> {
                    advanced.setExpanded(false);
                    digits.getValueFactory().setValue(6);
                    algorithm.setValue(TOTP.DEFAULT_ALGORITHM);
                    period.getValueFactory().setValue(30);

                    name.requestFocus();
                }));

                vBox = new VBox(10, nameLabel, name, secretLabel, secret, advanced);
            }
            case URL -> {
                TextField url = new TextField();
                Label urlLabel = new Label("URL:");
                urlLabel.setLabelFor(url);

                getDialogPane().lookupButton(ButtonType.OK).disableProperty().bind(new BooleanBinding() {
                    {
                        super.bind(url.textProperty());
                    }

                    @Override
                    protected boolean computeValue() {
                        return url.getText().isBlank() || !url.getText().trim().startsWith("otpauth://totp/");
                    }
                });



                setResultConverter(buttonType -> {
                    if (buttonType == ButtonType.OK) {
                        return TotpData.parseUrl(url.getText());
                    }
                    return null;
                });

                setOnShowing(e -> Platform.runLater(url::requestFocus));

                vBox = new VBox(urlLabel, url);
            }
            default -> vBox = new VBox();
        }

        getDialogPane().setContent(vBox);
    }

    public enum Type {
        MANUAL,
        URL
    }
}
