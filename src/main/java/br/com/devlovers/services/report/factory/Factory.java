package br.com.devlovers.services.report.factory;

import br.com.devlovers.services.report.model.Document;
import br.com.devlovers.services.report.model.Template;

public class Factory {

    public static Document createDoc(String templatePath) {
        return new Template(templatePath);
    }
}