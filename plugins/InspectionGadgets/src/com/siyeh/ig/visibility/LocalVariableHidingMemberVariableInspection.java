/*
 * Copyright 2003-2005 Dave Griffith
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.siyeh.ig.visibility;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.siyeh.InspectionGadgetsBundle;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.MethodInspection;
import com.siyeh.ig.fixes.RenameFix;
import com.siyeh.ig.psiutils.ClassUtils;
import com.siyeh.ig.ui.MultipleCheckboxOptionsPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;

public class LocalVariableHidingMemberVariableInspection
        extends MethodInspection {

    /** @noinspection PublicField*/
    public boolean m_ignoreInvisibleFields = true;
    /** @noinspection PublicField*/
    public boolean m_ignoreStaticMethods = true;
    private final RenameFix fix = new RenameFix();

    public String getID(){
        return "LocalVariableHidesMemberVariable";
    }

    public String getDisplayName() {
        return InspectionGadgetsBundle.message(
                "local.variable.hides.member.variable.display.name");
    }

    public String getGroupDisplayName() {
        return GroupNames.VISIBILITY_GROUP_NAME;
    }

    protected InspectionGadgetsFix buildFix(PsiElement location) {
        return fix;
    }

    protected boolean buildQuickFixesOnlyForOnTheFlyErrors() {
        return true;
    }

    public String buildErrorString(PsiElement location) {
        return InspectionGadgetsBundle.message(
                "local.variable.hides.member.variable.problem.descriptor");
    }

    public JComponent createOptionsPanel() {
        final MultipleCheckboxOptionsPanel optionsPanel =
                new MultipleCheckboxOptionsPanel(this);
        optionsPanel.addCheckbox(InspectionGadgetsBundle.message(
                "field.name.hides.in.superclass.ignore.option"),
                "m_ignoreInvisibleFields");
        optionsPanel.addCheckbox(InspectionGadgetsBundle.message(
                "local.variable.hides.member.variable.ignore.option"),
                "m_ignoreStaticMethods");
        return optionsPanel;
    }

    public BaseInspectionVisitor buildVisitor() {
        return new LocalVariableHidingMemberVariableVisitor();
    }

    private class LocalVariableHidingMemberVariableVisitor
            extends BaseInspectionVisitor {

        public void visitLocalVariable(@NotNull PsiLocalVariable variable) {
            super.visitLocalVariable(variable);
            if (m_ignoreStaticMethods) {
                final PsiMethod aMethod =
                        PsiTreeUtil.getParentOfType(variable,
                                PsiMethod.class);
                if (aMethod == null) {
                    return;
                }
                if (aMethod.hasModifierProperty(PsiModifier.STATIC)) {
                    return;
                }
            }
            final PsiClass aClass =
                    ClassUtils.getContainingClass(variable);
            if (aClass == null) {
                return;
            }
            final String variableName = variable.getName();
            final PsiField[] fields = aClass.getAllFields();
            for(final PsiField field : fields){
                if(checkFieldName(field, variableName, aClass)){
                    registerVariableError(variable);
                }
            }
        }

        public void visitParameter(@NotNull PsiParameter variable) {
            super.visitParameter(variable);
            if (!(variable.getDeclarationScope() instanceof PsiCatchSection)) {
                return;
            }
            final PsiClass aClass =
                    ClassUtils.getContainingClass(variable);
            if (aClass == null) {
                return;
            }
            final String variableName = variable.getName();
            final PsiField[] fields = aClass.getAllFields();
            for(final PsiField field : fields){
                if(checkFieldName(field, variableName, aClass)){
                    registerVariableError(variable);
                }
            }
        }

        private boolean checkFieldName(PsiField field, String variableName,
                                       PsiClass aClass) {
            if (field == null) {
                return false;
            }
            final String fieldName = field.getName();
            if (fieldName == null) {
                return false;
            }
            if (!fieldName.equals(variableName)) {
                return false;
            }
            return !m_ignoreInvisibleFields ||
                    ClassUtils.isFieldVisible(field, aClass);
        }
    }
}