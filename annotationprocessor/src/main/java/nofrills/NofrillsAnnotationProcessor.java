package nofrills;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes({"nofrills.events.EventListener"})
@SupportedSourceVersion(SourceVersion.RELEASE_25)
public class NofrillsAnnotationProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }
        List<String> classes = annotations.stream().flatMap(
                annotation -> roundEnv.getElementsAnnotatedWith(annotation).stream())
                .map(s -> ((TypeElement) s).getQualifiedName() + ".class").toList();
        try (Writer writer = processingEnv.getFiler().createSourceFile("nofrills.GeneratedAnnotationVoodoo").openWriter()) {
            writer.write("package nofrills;\n");
            writer.write("import java.util.List;\n");
            writer.write("public class GeneratedAnnotationVoodoo {\n");
            writer.write("static List<Class<?>> eventListeners = List.of(");
            writer.write(String.join(", ", classes));
            writer.write(");\n");
            writer.write("}\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
