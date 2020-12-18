/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.bean;

import static com.google.cloud.healthcare.fdamystudies.common.ColumnConstraints.SMALL_LENGTH;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ConsentStatusBean {

  @NotBlank private String studyId;
  private Boolean eligibility;
  @Valid @NotNull private ConsentReqBean consent;

  @Size(max = SMALL_LENGTH)
  private String sharing;

  public Boolean getEligibility() {
    return eligibility;
  }

  public void setEligibility(Boolean eligibility) {
    this.eligibility = eligibility;
  }

  public ConsentReqBean getConsent() {
    return consent;
  }

  public void setConsent(ConsentReqBean consent) {
    this.consent = consent;
  }

  public String getSharing() {
    return sharing;
  }

  public void setSharing(String sharing) {
    this.sharing = sharing;
  }
}
